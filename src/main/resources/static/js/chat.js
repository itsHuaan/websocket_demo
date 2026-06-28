    let stompClient = null;
    let connected = false;
    let currentUser = null;
    let activeRecipientId = null;
    let selectedFiles = [];
    let onlineUsers = new Set();
    let isTypingTimeout = null;
    let activeContactUsername = null;
    let activeContact = null; // full user object of the open conversation (for the info panel)
    let replyingTo = null; // { messageId, senderName, snippet }
    let contactsCache = []; // raw contact list, used by the forward picker
    let menuTarget = null;  // message the action popup currently targets
    let menuActiveEl = null; // the .message element whose kebab is active

    // DOM elements
    const authSection = document.getElementById('authSection');
    const chatSection = document.getElementById('chatSection');
    const userDisplay = document.getElementById('userDisplay');
    const currentUsernameSpan = document.getElementById('currentUsername');
    const profileAvatar = document.getElementById('profileAvatar');
    const profileMenu = document.getElementById('profileMenu');
    const themeLabel = document.getElementById('themeLabel');
    const themeState = document.getElementById('themeState');

    const userListElement = document.getElementById('userList');
    const activeChatHeader = document.getElementById('activeChatHeader');
    const chatInputArea = document.getElementById('chatInputArea');
    const messageInput = document.getElementById('messageInput');
    const mediaFilesInput = document.getElementById('mediaFiles');
    const chatMessages = document.getElementById('chat-messages');
    const connectionIndicator = document.getElementById('connectionIndicator');
    const connectionText = document.getElementById('connectionText');
    const sendButton = document.getElementById('sendButton');
    const fileCount = document.getElementById('fileCount');
    const filePreviewContainer = document.getElementById('filePreviewContainer');
    const chatInfoBtn = document.getElementById('chatInfoBtn');
    const infoAvatar = document.getElementById('infoAvatar');
    const infoName = document.getElementById('infoName');
    const infoUsername = document.getElementById('infoUsername');
    let typingBubbleEl = null; // the animated "is typing" bubble, appended into the message list

    // Show a received-style bubble with animated dots at the bottom of the conversation
    function showTypingBubble() {
        if (!typingBubbleEl) {
            typingBubbleEl = document.createElement('div');
            typingBubbleEl.className = 'typing-bubble';
            typingBubbleEl.innerHTML = '<div class="typing-dots"><span></span><span></span><span></span></div>';
        }
        // Already visible? Do nothing — re-appending restarts the animation and flickers
        if (typingBubbleEl.parentNode === chatMessages) return;
        chatMessages.appendChild(typingBubbleEl);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function hideTypingBubble() {
        if (typingBubbleEl && typingBubbleEl.parentNode) {
            typingBubbleEl.parentNode.removeChild(typingBubbleEl);
        }
    }
    const replyBar = document.getElementById('replyBar');
    const replyBarSender = document.getElementById('replyBarSender');
    const replyBarText = document.getElementById('replyBarText');
    const msgMenu = document.getElementById('msgMenu');
    const forwardModal = document.getElementById('forwardModal');
    const forwardList = document.getElementById('forwardList');
    const forwardPreview = document.getElementById('forwardPreview');

    // Check for existing session
    window.onload = function() {
        initTheme();

        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        const refreshToken = urlParams.get('refreshToken');
        
        if (token && refreshToken) {
            currentUser = { token, refreshToken };
            authFetch('/v1/api/users/me')
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200 && data.data) {
                        currentUser = { 
                            ...data.data, 
                            token, 
                            refreshToken, 
                            id: data.data.userId || data.data.id 
                        };
                        localStorage.setItem('chat_user', JSON.stringify(currentUser));
                        window.history.replaceState({}, document.title, window.location.pathname);
                        showChat();
                    } else {
                        showAuth();
                    }
                }).catch(() => showAuth());
        } else {
            const savedUser = localStorage.getItem('chat_user');
            if (savedUser) {
                currentUser = JSON.parse(savedUser);
                showChat();
            }
        }
    };

    // ===== Dark mode =====
    // persist=false applies a theme without remembering it (used for the OS default,
    // so an untouched session keeps following the machine theme).
    function applyTheme(theme, persist = true) {
        const isDark = theme === 'dark';
        document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
        if (persist) localStorage.setItem('chat_theme', isDark ? 'dark' : 'light');
        if (themeLabel) themeLabel.textContent = isDark ? 'Light mode' : 'Dark mode';
        if (themeState) themeState.textContent = isDark ? 'On' : 'Off';
    }
    // Stored choice wins; otherwise follow the OS theme (and keep following it live
    // until the user makes their own choice).
    function initTheme() {
        const stored = localStorage.getItem('chat_theme');
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        applyTheme(stored || (prefersDark ? 'dark' : 'light'), false);
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (!localStorage.getItem('chat_theme')) applyTheme(e.matches ? 'dark' : 'light', false);
            });
        }
    }

    function toggleDarkMode() {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        applyTheme(isDark ? 'light' : 'dark');
    }

    // ===== Profile popup menu =====
    function toggleProfileMenu(event) {
        if (event) event.stopPropagation();
        const open = profileMenu.classList.toggle('open');
        userDisplay.classList.toggle('menu-open', open);
    }

    function closeProfileMenu() {
        profileMenu.classList.remove('open');
        userDisplay.classList.remove('menu-open');
    }

    // Close the menu when clicking anywhere outside it
    document.addEventListener('click', function(e) {
        if (profileMenu.classList.contains('open') &&
            !profileMenu.contains(e.target) && !userDisplay.contains(e.target)) {
            closeProfileMenu();
        }
    });
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') closeProfileMenu();
    });

    // ===== Settings: edit profile =====
    const settingsModal = document.getElementById('settingsModal');
    const settingsAvatar = document.getElementById('settingsAvatar');
    const settingsFirstName = document.getElementById('settingsFirstName');
    const settingsLastName = document.getElementById('settingsLastName');
    const settingsUsernameInput = document.getElementById('settingsUsername');
    const settingsPhotoInput = document.getElementById('settingsPhotoInput');
    const settingsSaveBtn = document.getElementById('settingsSaveBtn');
    let settingsPhotoFile = null; // newly chosen image, if any
    let settingsPhotoUrl = null;  // object URL for its preview (revoked on close)

    function openSettings() {
        closeProfileMenu();
        if (!currentUser) return;
        settingsPhotoFile = null;
        if (settingsPhotoUrl) { URL.revokeObjectURL(settingsPhotoUrl); settingsPhotoUrl = null; }
        settingsPhotoInput.value = '';
        settingsFirstName.value = currentUser.firstName || '';
        settingsLastName.value = currentUser.lastName || '';
        settingsUsernameInput.value = currentUser.username || '';

        const hintDiv = settingsUsernameInput.nextElementSibling;
        if (currentUser.lastUsernameChangeDate) {
            const lastChange = new Date(currentUser.lastUsernameChangeDate);
            const now = new Date();
            const diffDays = (now - lastChange) / (1000 * 60 * 60 * 24);
            if (diffDays < 30) {
                const daysLeft = Math.ceil(30 - diffDays);
                settingsUsernameInput.disabled = true;
                if (hintDiv && hintDiv.classList.contains('form-hint')) {
                    hintDiv.textContent = `You can change your username again in ${daysLeft} days.`;
                    hintDiv.style.color = '#ef4444';
                }
            } else {
                settingsUsernameInput.disabled = false;
                if (hintDiv && hintDiv.classList.contains('form-hint')) {
                    hintDiv.textContent = 'You can change your username once every 30 days.';
                    hintDiv.style.color = '';
                }
            }
        } else {
            settingsUsernameInput.disabled = false;
            if (hintDiv && hintDiv.classList.contains('form-hint')) {
                hintDiv.textContent = 'You can change your username once every 30 days.';
                hintDiv.style.color = '';
            }
        }

        setFieldError('settingsError', '');
        renderSettingsAvatar();
        settingsModal.classList.add('open');
    }

    function closeSettings() {
        settingsModal.classList.remove('open');
        if (settingsPhotoUrl) { URL.revokeObjectURL(settingsPhotoUrl); settingsPhotoUrl = null; }
        settingsPhotoFile = null;
    }

    function renderSettingsAvatar() {
        const img = settingsPhotoFile ? settingsPhotoUrl : (currentUser && currentUser.profilePicture);
        if (img) {
            settingsAvatar.textContent = '';
            settingsAvatar.style.backgroundImage = `url("${img}")`;
            settingsAvatar.classList.add('has-image');
        } else {
            settingsAvatar.style.backgroundImage = '';
            settingsAvatar.classList.remove('has-image');
            settingsAvatar.textContent = (displayName(currentUser) || '?').charAt(0).toUpperCase();
        }
    }

    document.getElementById('settingsPhotoBtn').addEventListener('click', () => settingsPhotoInput.click());
    settingsPhotoInput.addEventListener('change', () => {
        const file = settingsPhotoInput.files[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) { setFieldError('settingsError', 'Please choose an image file.'); return; }
        if (settingsPhotoUrl) URL.revokeObjectURL(settingsPhotoUrl);
        settingsPhotoFile = file;
        settingsPhotoUrl = URL.createObjectURL(file);
        setFieldError('settingsError', '');
        renderSettingsAvatar();
    });

    settingsModal.addEventListener('click', e => { if (e.target === settingsModal) closeSettings(); });

    function saveSettings() {
        const firstName = settingsFirstName.value.trim();
        const lastName = settingsLastName.value.trim();
        const username = settingsUsernameInput.value.trim();
        if (!firstName || !lastName) return setFieldError('settingsError', 'Please enter your first and last name.');
        if (!username) return setFieldError('settingsError', 'Please enter a username.');
        setFieldError('settingsError', '');

        const form = new FormData();
        form.append('firstName', firstName);
        form.append('lastName', lastName);
        if (username !== currentUser.username) form.append('username', username);
        if (settingsPhotoFile) form.append('profilePicture', settingsPhotoFile);

        settingsSaveBtn.disabled = true;
        const idle = settingsSaveBtn.innerHTML;
        settingsSaveBtn.innerHTML = '<span class="btn-loading"><span class="spinner"></span>Saving…</span>';

        authFetch('/v1/api/users/me', { method: 'PUT', body: form })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                currentUser.firstName = data.data.firstName;
                currentUser.lastName = data.data.lastName;
                currentUser.username = data.data.username;
                currentUser.profilePicture = data.data.profilePicture;
                currentUser.lastUsernameChangeDate = data.data.lastUsernameChangeDate;
                localStorage.setItem('chat_user', JSON.stringify(currentUser));
                currentUsernameSpan.textContent = displayName(currentUser);
                renderProfileAvatar();
                closeSettings();
                showDialog({ title: 'Profile updated', message: 'Your profile has been saved.', confirmText: 'Done' });
            } else {
                setFieldError('settingsError', data.message || 'Could not update your profile.');
            }
        })
        .catch(() => setFieldError('settingsError', 'Something went wrong. Please try again.'))
        .finally(() => { settingsSaveBtn.disabled = false; settingsSaveBtn.innerHTML = idle; });
    }

    // ===== Styled dialog (replaces native alert) =====
    const appDialog = document.getElementById('appDialog');
    const appDialogIcon = document.getElementById('appDialogIcon');
    const appDialogTitle = document.getElementById('appDialogTitle');
    const appDialogMessage = document.getElementById('appDialogMessage');
    const appDialogActions = document.getElementById('appDialogActions');

    const DIALOG_ICONS = {
        info: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>',
        warn: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>'
    };

    // Styled replacement for alert()/confirm(): showDialog({ title, message, confirmText,
    // cancelText, icon: 'info'|'warn', danger, onConfirm }). A cancelText turns it into a confirm.
    function showDialog({ title = 'Notice', message = '', confirmText = 'OK', cancelText = null, icon = 'info', danger = false, onConfirm = null } = {}) {
        appDialogIcon.className = 'dialog-icon' + (icon === 'warn' ? ' warn' : '');
        appDialogIcon.innerHTML = DIALOG_ICONS[icon] || DIALOG_ICONS.info;
        appDialogTitle.textContent = title;
        appDialogMessage.textContent = message;
        appDialogActions.innerHTML = '';

        if (cancelText) {
            const cancel = document.createElement('button');
            cancel.className = 'dialog-btn';
            cancel.textContent = cancelText;
            cancel.onclick = () => closeDialog();
            appDialogActions.appendChild(cancel);
        }

        const ok = document.createElement('button');
        ok.className = 'dialog-btn primary' + (danger ? ' danger' : '');
        ok.textContent = confirmText;
        ok.onclick = () => { closeDialog(); if (onConfirm) onConfirm(); };
        appDialogActions.appendChild(ok);

        appDialog.classList.add('open');
        requestAnimationFrame(() => ok.focus());
    }

    function closeDialog() {
        appDialog.classList.remove('open');
    }

    appDialog.addEventListener('click', e => { if (e.target === appDialog) closeDialog(); });

    // Full name (first + last) with a graceful fallback to the username
    function displayName(user) {
        if (!user) return 'Unknown';
        const full = [user.firstName, user.lastName].filter(s => s && s.trim()).join(' ').trim();
        return full || user.username || 'Unknown';
    }

    // ===== Avatar rendering (profile picture, else first-letter fallback) =====
    function fillAvatar(el, user) {
        const initial = (displayName(user) || '?').charAt(0).toUpperCase();
        if (user.profilePicture) {
            el.textContent = '';
            el.style.backgroundImage = `url("${user.profilePicture}")`;
            el.classList.add('has-image');
        } else {
            el.style.backgroundImage = '';
            el.classList.remove('has-image');
            el.textContent = initial;
        }
    }

    // ===== Login =====
    const loginButton = document.getElementById('loginButton');

    function setLoginLoading(loading) {
        loginButton.disabled = loading;
        loginButton.innerHTML = loading
            ? '<span class="btn-loading"><span class="spinner"></span>Signing in…</span>'
            : 'Sign In';
    }

    function login() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        setLoginLoading(true);

        fetch('/v1/api/auth/sign-in', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                currentUser = data.data; // SignInResponse
                localStorage.setItem('chat_user', JSON.stringify(currentUser));
                showChat();
            } else {
                showDialog({ title: 'Sign in failed', message: data.message || 'Invalid credentials. Please try again.', icon: 'warn' });
            }
        })
        .catch(error => {
            console.error("Login error:", error);
            showDialog({ title: 'Something went wrong', message: 'An error occurred during login. Please try again.', icon: 'warn' });
        })
        .finally(() => setLoginLoading(false));
    }

    // Toggle the password field within the given toggle button's wrapper
    function togglePassword(btn) {
        const input = btn.parentElement.querySelector('input');
        if (!input) return;
        const reveal = input.type === 'password';
        input.type = reveal ? 'text' : 'password';
        btn.setAttribute('aria-label', reveal ? 'Hide password' : 'Show password');
        btn.innerHTML = reveal
            ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>'
            : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
    }

    // ===== Sign up / email verification =====
    let pendingEmail = null;    // email awaiting OTP verification
    let pendingUsername = null; // prefill on the login screen after verifying
    let resendTimer = null;
    const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const VN_PHONE_RE = /^(?:\+?84|0)?[35789]\d{8}$/;

    function showLoginView() { switchAuthView('loginView'); }
    function showSignupView() { switchAuthView('signupView'); }
    function showOtpView() { switchAuthView('otpView'); }

    function switchAuthView(viewId) {
        ['loginView', 'signupView', 'otpView'].forEach(id => {
            document.getElementById(id).style.display = id === viewId ? 'block' : 'none';
        });
        setFieldError('signupError', '');
        setFieldError('otpError', '');
    }

    function setFieldError(id, msg) {
        const el = document.getElementById(id);
        if (!el) return;
        el.textContent = msg || '';
        el.style.display = msg ? 'block' : 'none';
    }

    function setBtnLoading(btnId, loading, idleHtml) {
        const btn = document.getElementById(btnId);
        btn.disabled = loading;
        btn.innerHTML = loading
            ? '<span class="btn-loading"><span class="spinner"></span>Please wait…</span>'
            : idleHtml;
    }

    // Resolve a human country name from an ISO code (e.g. "VN" -> "Vietnam") using the
    // browser's built-in Intl data, falling back to the ISO code itself.
    const regionNames = (() => {
        try { return new Intl.DisplayNames(['en'], { type: 'region' }); } catch (e) { return null; }
    })();
    function countryName(iso) {
        try { return (regionNames && regionNames.of(iso)) || iso; } catch (e) { return iso; }
    }

    // Turn a raw dial value from the API (e.g. "84", "+1-246", "+358-18", " ") into a
    // clean international prefix ("+84", "+1", "+358"); returns '' when there's no code.
    function normalizeDial(raw) {
        const m = String(raw || '').trim().match(/\+?\d+/);
        return m ? '+' + m[0].replace('+', '') : '';
    }

    // Build the sign-up country-code picker (custom dropdown with image flags) from the
    // helper API's ISO -> dial-code map. The static VN trigger stays as a fallback if the
    // request fails. The selected dial code lives on #suPhoneCode's data-dial attribute.
    function loadPhoneCodes() {
        const wrap = document.getElementById('suPhoneCode');
        if (!wrap) return;
        const trigger = document.getElementById('suPhoneCodeTrigger');
        const menu = document.getElementById('suPhoneCodeMenu');
        const list = document.getElementById('suPhoneCodeList');
        const search = document.getElementById('suPhoneCodeSearch');
        const flagEl = document.getElementById('suPhoneCodeFlag');
        const dialEl = document.getElementById('suPhoneCodeDial');

        const closeMenu = () => { menu.classList.remove('open'); trigger.setAttribute('aria-expanded', 'false'); };
        const openMenu = () => {
            menu.classList.add('open');
            trigger.setAttribute('aria-expanded', 'true');
            search.value = '';
            filter('');
            requestAnimationFrame(() => search.focus());
        };

        function selectCode(dial, iso) {
            wrap.dataset.dial = dial;
            flagEl.className = 'fi fi-' + iso.toLowerCase();
            dialEl.textContent = dial;
            list.querySelectorAll('.phone-code-option').forEach(o => o.classList.toggle('active', o.dataset.dial === dial && o.dataset.iso === iso.toLowerCase()));
        }

        function filter(q) {
            const needle = q.trim().toLowerCase();
            list.querySelectorAll('.phone-code-option').forEach(li => {
                const hay = li.dataset.name + ' ' + li.dataset.dial + ' ' + li.dataset.iso;
                li.style.display = hay.includes(needle) ? '' : 'none';
            });
        }

        trigger.addEventListener('click', e => {
            e.stopPropagation();
            if (!list.children.length) return;
            menu.classList.contains('open') ? closeMenu() : openMenu();
        });
        search.addEventListener('input', () => filter(search.value));
        document.addEventListener('click', e => { if (!wrap.contains(e.target)) closeMenu(); });
        document.addEventListener('keydown', e => { if (e.key === 'Escape') closeMenu(); });

        fetch('/v1/api/helper/phone-codes')
            .then(res => res.json())
            .then(data => {
                const map = (data && data.data) || {};
                const entries = Object.keys(map)
                    .map(iso => ({ iso: iso.toLowerCase(), dial: normalizeDial(map[iso]), name: countryName(iso) }))
                    .filter(e => e.dial)
                    .sort((a, b) => a.name.localeCompare(b.name));
                if (!entries.length) return;

                list.innerHTML = entries.map(e =>
                    `<li class="phone-code-option" role="option" data-dial="${e.dial}" data-iso="${e.iso}" data-name="${e.name.toLowerCase()}">`
                    + `<span class="fi fi-${e.iso}"></span>`
                    + `<span class="phone-code-country">${e.name}</span>`
                    + `<span class="phone-code-dial">${e.dial}</span></li>`).join('');

                list.querySelectorAll('.phone-code-option').forEach(opt => {
                    opt.addEventListener('click', () => { selectCode(opt.dataset.dial, opt.dataset.iso); closeMenu(); });
                });

                // Default to Vietnam if present, otherwise the first country alphabetically.
                const def = entries.find(e => e.iso === 'vn') || entries[0];
                selectCode(def.dial, def.iso);
            })
            .catch(() => { /* keep the fallback VN trigger; phone is optional anyway */ });
    }

    function signup() {
        const firstName = document.getElementById('suFirstName').value.trim();
        const lastName = document.getElementById('suLastName').value.trim();
        const email = document.getElementById('suEmail').value.trim();
        const username = document.getElementById('suUsername').value.trim();
        const phoneLocal = document.getElementById('suPhone').value.trim();
        const phoneCode = document.getElementById('suPhoneCode').dataset.dial || '+84';
        const password = document.getElementById('suPassword').value;
        const confirm = document.getElementById('suConfirm').value;

        if (!firstName || !lastName) return setFieldError('signupError', 'Please enter your first and last name.');
        if (!EMAIL_RE.test(email)) return setFieldError('signupError', 'Please enter a valid email address.');
        if (username.length < 3) return setFieldError('signupError', 'Username must be at least 3 characters.');

        // Combine the selected dial code with the national number (digits only, leading
        // zero dropped). Only Vietnam is offered today, so we validate against its format.
        let phoneNumber = '';
        if (phoneLocal) {
            const national = phoneLocal.replace(/\D/g, '').replace(/^0+/, '');
            phoneNumber = phoneCode + national;
            if (phoneCode === '+84' && !VN_PHONE_RE.test(phoneNumber)) {
                return setFieldError('signupError', 'Please enter a valid phone number.');
            }
        }

        if (password.length < 6) return setFieldError('signupError', 'Password must be at least 6 characters.');
        if (password !== confirm) return setFieldError('signupError', 'Passwords do not match.');
        setFieldError('signupError', '');

        const payload = { firstName, lastName, email, username, password };
        if (phoneNumber) payload.phoneNumber = phoneNumber;

        setBtnLoading('signupButton', true);
        fetch('/v1/api/auth/sign-up', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 201 || data.code === 200) {
                pendingEmail = email;
                pendingUsername = username;
                document.getElementById('otpEmail').textContent = email;
                clearOtpBoxes();
                showOtpView();
                requestAnimationFrame(() => otpBoxes[0].focus());
                startResendCooldown();
            } else {
                setFieldError('signupError', data.message || 'Sign up failed. Please try again.');
            }
        })
        .catch(() => setFieldError('signupError', 'Something went wrong. Please try again.'))
        .finally(() => setBtnLoading('signupButton', false, 'Create account'));
    }

    function verifyOtp() {
        const otp = getOtpValue();
        if (!/^\d{6}$/.test(otp)) return setFieldError('otpError', 'Enter the 6-digit code.');
        setFieldError('otpError', '');

        setBtnLoading('otpButton', true);
        fetch('/v1/api/auth/verify-sign-up', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingEmail, otp })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                if (resendTimer) clearInterval(resendTimer);
                showLoginView();
                if (pendingUsername) document.getElementById('username').value = pendingUsername;
                document.getElementById('password').value = '';
                showDialog({ title: 'Account verified', message: 'Your account is ready. Please sign in.', confirmText: 'Sign in' });
            } else {
                setFieldError('otpError', data.message || 'Invalid or expired code.');
            }
        })
        .catch(() => setFieldError('otpError', 'Something went wrong. Please try again.'))
        .finally(() => setBtnLoading('otpButton', false, 'Verify'));
    }

    function resendOtp() {
        if (!pendingEmail) return;
        const btn = document.getElementById('resendBtn');
        if (btn.disabled) return;
        fetch('/v1/api/auth/resend-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingEmail })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setFieldError('otpError', '');
                startResendCooldown();
            } else {
                setFieldError('otpError', data.message || 'Could not resend the code.');
            }
        })
        .catch(() => setFieldError('otpError', 'Could not resend the code.'));
    }

    // Briefly disable the resend link with a countdown to avoid spamming emails
    function startResendCooldown() {
        const btn = document.getElementById('resendBtn');
        let seconds = 30;
        btn.disabled = true;
        const tick = () => {
            if (seconds <= 0) {
                clearInterval(resendTimer);
                btn.disabled = false;
                btn.textContent = 'Resend code';
                return;
            }
            btn.textContent = `Resend code (${seconds}s)`;
            seconds--;
        };
        tick();
        if (resendTimer) clearInterval(resendTimer);
        resendTimer = setInterval(tick, 1000);
    }

    // ===== Segmented OTP input =====
    const otpBoxes = Array.from(document.querySelectorAll('#otpGroup .otp-box'));

    function getOtpValue() {
        return otpBoxes.map(b => b.value).join('');
    }
    function clearOtpBoxes() {
        otpBoxes.forEach(b => { b.value = ''; b.classList.remove('filled'); });
    }

    otpBoxes.forEach((box, i) => {
        box.addEventListener('input', () => {
            box.value = box.value.replace(/\D/g, '').slice(0, 1);
            box.classList.toggle('filled', box.value !== '');
            setFieldError('otpError', '');
            if (box.value && i < otpBoxes.length - 1) otpBoxes[i + 1].focus();
        });
        box.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                verifyOtp();
            } else if (e.key === 'Backspace' && !box.value && i > 0) {
                e.preventDefault();
                otpBoxes[i - 1].focus();
                otpBoxes[i - 1].value = '';
                otpBoxes[i - 1].classList.remove('filled');
            } else if (e.key === 'ArrowLeft' && i > 0) {
                e.preventDefault();
                otpBoxes[i - 1].focus();
            } else if (e.key === 'ArrowRight' && i < otpBoxes.length - 1) {
                e.preventDefault();
                otpBoxes[i + 1].focus();
            }
        });
        box.addEventListener('paste', (e) => {
            e.preventDefault();
            const digits = (e.clipboardData.getData('text') || '').replace(/\D/g, '').slice(0, otpBoxes.length);
            if (!digits) return;
            digits.split('').forEach((d, j) => {
                otpBoxes[j].value = d;
                otpBoxes[j].classList.add('filled');
            });
            otpBoxes[Math.min(digits.length, otpBoxes.length - 1)].focus();
        });
    });

    function logout() {
        closeProfileMenu();
        // Best-effort: revoke the refresh token on the server so it can't be reused
        const refreshToken = currentUser && currentUser.refreshToken;
        if (refreshToken) {
            fetch('/v1/api/auth/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            }).catch(() => { /* ignore — logging out locally regardless */ });
        }
        if (stompClient) {
            stompClient.disconnect();
        }
        localStorage.removeItem('chat_user');
        currentUser = null;
        activeRecipientId = null;
        showAuth();
    }

    function showChat() {
        authSection.style.display = 'none';
        chatSection.style.display = 'flex';
        userDisplay.style.display = 'flex';
        currentUsernameSpan.textContent = displayName(currentUser);
        renderProfileAvatar();

        loadOnlineUsers().then(() => {
            if (!currentUser) return; // session was rejected during the online-users fetch
            loadContacts();
            connect();
        });
    }

    // Show the user's profile picture if they have one, else their initial
    function renderProfileAvatar() {
        if (!currentUser) return;
        fillAvatar(profileAvatar, currentUser);
    }

    function showAuth() {
        authSection.style.display = 'block';
        chatSection.style.display = 'none';
        chatSection.classList.remove('chat-active');
        chatSection.classList.remove('has-active-chat');
        chatSection.classList.remove('info-open');
        chatInfoBtn.style.display = 'none';
        activeContact = null;
        userDisplay.style.display = 'none';
        updateConnectionStatus(false);
    }

    // Clear the session and bounce to login, showing a dialog
    function forceLogout({ title = 'Session expired', message = 'Your session has expired. Please sign in again.' } = {}) {
        if (!currentUser) return; // already handled
        if (stompClient) {
            try { stompClient.disconnect(); } catch (e) { /* ignore */ }
        }
        localStorage.removeItem('chat_user');
        currentUser = null;
        activeRecipientId = null;
        showAuth();
        showDialog({ title, message, confirmText: 'Sign in', icon: 'warn' });
    }

    // Token was rejected (401) and couldn't be refreshed. customMessage carries the
    // server's reason when present (e.g. the account was locked while offline).
    function handleSessionExpired(customMessage) {
        if (customMessage) forceLogout({ title: 'Account update', message: customMessage });
        else forceLogout();
    }

    // Real-time push: an admin changed this account's status. A non-active status
    // logs the user out immediately with the admin's reason.
    function handleAccountStatus(data) {
        if (!data) return;
        if (data.action === 'DELETED') {
            forceLogout({ title: 'Account deleted', message: 'An administrator has deleted your account.' });
            return;
        }
        if (data.action === 'UPDATED') {
            forceLogout({ title: 'Account updated', message: 'An administrator has modified your account. For security reasons, please log in again.' });
            return;
        }
        if (data.status === 1) return; // reactivated — nothing to do
        const reason = (data.reason || '').trim();
        forceLogout({
            title: 'Account locked',
            message: reason
                ? 'An administrator has locked your account: ' + reason
                : 'An administrator has locked your account.'
        });
    }

    // Holds an in-flight refresh so concurrent 401s share one refresh call
    let refreshPromise = null;
    let lastAuthError = null; // server's reason when a refresh is rejected

    // Exchange the stored refresh token for a fresh access/refresh pair.
    // Resolves true on success (currentUser + storage updated), false otherwise.
    function refreshAccessToken() {
        if (refreshPromise) return refreshPromise;
        if (!currentUser || !currentUser.refreshToken) return Promise.resolve(false);
        refreshPromise = fetch('/v1/api/auth/refresh-token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: currentUser.refreshToken })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200 && data.data && data.data.token) {
                currentUser = data.data; // new SignInResponse (rotated refresh token included)
                localStorage.setItem('chat_user', JSON.stringify(currentUser));
                lastAuthError = null;
                return true;
            }
            lastAuthError = data.message || null; // e.g. "Your account has been locked: …"
            return false;
        })
        .catch(() => false)
        .finally(() => { refreshPromise = null; });
        return refreshPromise;
    }

    // fetch wrapper that injects the bearer token, silently refreshes an expired
    // token (once) and retries, and only bounces to login if the refresh fails
    function authFetch(url, options = {}, retried = false) {
        if (!currentUser) return Promise.reject(new Error('Not authenticated'));
        const opts = { ...options };
        opts.headers = { ...(options.headers || {}), 'Authorization': 'Bearer ' + currentUser.token };
        return fetch(url, opts).then(response => {
            if (response.status !== 401) return response;
            if (retried) { // already tried a fresh token — give up
                handleSessionExpired();
                throw new Error('Session expired');
            }
            return refreshAccessToken().then(ok => {
                if (ok) return authFetch(url, options, true);
                handleSessionExpired(lastAuthError);
                throw new Error('Session expired');
            });
        });
    }

    function loadOnlineUsers() {
        return authFetch('/v1/api/users/online', { method: 'GET' })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                onlineUsers = new Set(data.data.map(id => String(id)));
            }
        })
        .catch(err => console.error("Failed to load online users:", err));
    }

    function loadContacts() {
        authFetch('/v1/api/users/contacts', { method: 'GET' })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                renderContacts(data.data || []);
            }
        })
        .catch(err => console.error("Failed to load contacts:", err));
    }

    function renderContacts(users) {
        contactsCache = users || [];
        userListElement.innerHTML = '';
        users.forEach(user => {
            if (user.userId == currentUser.id) return; // Don't show self

            const isOnline = onlineUsers.has(String(user.userId));

            const userItem = document.createElement('div');
            userItem.className = 'user-item';
            userItem.id = `user-${user.userId}`;
            userItem.dataset.search = `${displayName(user)} ${user.username || ''}`.toLowerCase();
            userItem.onclick = () => selectContact(user);
            if (activeRecipientId == user.userId) userItem.classList.add('active');

            const avatarContainer = document.createElement('div');
            avatarContainer.className = 'user-avatar-container';

            const avatar = document.createElement('div');
            avatar.className = 'user-avatar';
            fillAvatar(avatar, user);

            const dot = document.createElement('div');
            dot.className = `online-dot ${isOnline ? 'online' : ''}`;
            dot.id = `dot-${user.userId}`;

            avatarContainer.appendChild(avatar);
            avatarContainer.appendChild(dot);

            const details = document.createElement('div');
            details.className = 'user-details';

            const name = document.createElement('div');
            name.className = 'user-name';
            name.textContent = displayName(user);

            // Latest message preview (filled asynchronously); the dot conveys presence
            const status = document.createElement('div');
            status.className = 'user-status';
            status.id = `status-text-${user.userId}`;
            status.textContent = '';

            details.appendChild(name);
            details.appendChild(status);
            userItem.appendChild(avatarContainer);
            userItem.appendChild(details);

            userListElement.appendChild(userItem);
        });

        loadContactPreviews();
        filterContacts(); // re-apply any active search term after a re-render
    }

    // Filter the contact list by the search box (matches display name + username)
    function filterContacts() {
        const input = document.getElementById('contactSearch');
        const term = (input ? input.value : '').trim().toLowerCase();
        let visible = 0;
        userListElement.querySelectorAll('.user-item').forEach(item => {
            const match = !term || (item.dataset.search || '').includes(term);
            item.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        const empty = document.getElementById('contactEmpty');
        if (empty) empty.style.display = (term && visible === 0) ? 'block' : 'none';
    }

    // Build a one-line preview from a message (prefixes "You: " for own messages)
    function formatPreview(msg, isOwn) {
        if (!msg) return 'No messages yet';
        const prefix = isOwn ? 'You: ' : '';
        const text = (msg.message || '').trim();
        if (text) return prefix + text;
        if (msg.mediaUrls && msg.mediaUrls.length) return prefix + '📷 Media';
        return 'No messages yet';
    }

    // Fetch the latest message for each contact and show it under their name
    function loadContactPreviews() {
        contactsCache.forEach(user => {
            if (user.userId == currentUser.id) return;
            authFetch(`/v1/api/message/${user.userId}?size=1`, { method: 'GET' })
                .then(res => res.json())
                .then(data => {
                    const el = document.getElementById(`status-text-${user.userId}`);
                    if (!el) return;
                    const last = data.code === 200 && data.data.content && data.data.content[0];
                    el.textContent = last ? formatPreview(last, last.senderId == currentUser.id) : 'No messages yet';
                })
                .catch(() => {});
        });
    }

    // Update a single contact's preview line in response to a live message
    function updateContactPreview(userId, msgData, isOwn) {
        const el = document.getElementById(`status-text-${userId}`);
        if (el) el.textContent = formatPreview(msgData, isOwn);
    }

    function selectContact(user) {
        // On mobile, reveal the chat detail pane (slides over the contact list)
        chatSection.classList.add('chat-active');
        chatSection.classList.add('has-active-chat'); // swaps the empty state for the message list
        if (activeRecipientId == user.userId) return;

        // Highlight active user
        document.querySelectorAll('.user-item').forEach(el => el.classList.remove('active'));
        const userEl = document.getElementById(`user-${user.userId}`);
        if (userEl) userEl.classList.add('active');

        activeRecipientId = user.userId;
        activeContact = user;
        activeContactUsername = displayName(user);
        cancelReply(); // a pending reply belongs to the previous conversation
        const isOnline = onlineUsers.has(String(user.userId));

        const headerName = displayName(user);
        const headerInitial = headerName.charAt(0).toUpperCase();
        const headerAvatar = user.profilePicture
            ? `<div class="user-avatar has-image" style="width: 30px; height: 30px; margin-right: 10px; background-image: url('${user.profilePicture}');"></div>`
            : `<div class="user-avatar" style="width: 30px; height: 30px; margin-right: 10px;">${headerInitial}</div>`;

        activeChatHeader.innerHTML = `
            <div class="header-user-info">
                ${headerAvatar}
                <div>
                    <div>${headerName}</div>
                    <div style="font-size: 0.7rem; font-weight: normal; color: ${isOnline ? 'var(--success-color)' : '#666'}">${isOnline ? 'Online' : 'Offline'}</div>
                </div>
            </div>
        `;

        chatInfoBtn.style.display = 'flex';
        if (chatSection.classList.contains('info-open')) populateInfoPanel();

        chatInputArea.style.display = 'flex';
        chatMessages.innerHTML = ''; // Clear current messages
        hideTypingBubble();
        updateSendButtonState();

        loadChatHistory(user.userId);
        sendReadReceipt(user.userId);
    }

    // Go back to the contact list. On mobile this slides the chat away; on a tight
    // desktop (where the contact list is hidden to make room for the info panel) it
    // closes the panel, which reveals the contact list again.
    function showContactList() {
        chatSection.classList.remove('chat-active');
        closeInfoPanel();
    }

    // ===== Contact info panel (right sidebar on desktop, full page on mobile) =====
    function toggleInfoPanel() {
        if (!activeContact) return;
        const open = chatSection.classList.toggle('info-open');
        chatInfoBtn.classList.toggle('active', open);
        if (open) populateInfoPanel();
    }

    function closeInfoPanel() {
        chatSection.classList.remove('info-open');
        chatInfoBtn.classList.remove('active');
    }

    function populateInfoPanel() {
        if (!activeContact) return;
        fillAvatar(infoAvatar, activeContact);
        infoName.textContent = displayName(activeContact);
        infoUsername.textContent = '@' + activeContact.username;
    }

    // ===== Reply =====
    function startReply(messageId, senderName, text, mediaUrls) {
        if (messageId == null) return;
        let snippet = (text && text.trim() !== '')
            ? text.trim()
            : ((mediaUrls && mediaUrls.length > 0) ? '📷 Media' : '');
        replyingTo = { messageId, senderName, snippet };
        renderReplyBar();
        messageInput.focus();
    }

    function cancelReply() {
        replyingTo = null;
        renderReplyBar();
    }

    function renderReplyBar() {
        if (!replyingTo) {
            replyBar.style.display = 'none';
            return;
        }
        replyBarSender.textContent = `Replying to ${replyingTo.senderName}`;
        replyBarText.textContent = replyingTo.snippet;
        replyBar.style.display = 'flex';
    }

    // ===== Message action popup (Reply / Forward) =====
    function openMsgMenu(event, target, btnEl) {
        event.stopPropagation();
        menuTarget = target;

        // Reply/Forward make no sense on a deleted tombstone; "Remove for everyone"
        // is only offered on your own (non-deleted) messages. Set before measuring.
        const canReplyForward = !target.deleted;
        document.getElementById('msgMenuReply').style.display = canReplyForward ? 'flex' : 'none';
        document.getElementById('msgMenuForward').style.display = canReplyForward ? 'flex' : 'none';
        document.getElementById('msgMenuRemoveEveryone').style.display = (target.isSender && !target.deleted) ? 'flex' : 'none';
        document.getElementById('msgMenuRemoveMe').style.display = 'flex';

        if (menuActiveEl) menuActiveEl.classList.remove('menu-active');
        menuActiveEl = btnEl.closest('.message');
        if (menuActiveEl) menuActiveEl.classList.add('menu-active');

        // Show first so we can measure, then clamp within the viewport
        msgMenu.classList.add('open');
        const rect = btnEl.getBoundingClientRect();
        const menuW = msgMenu.offsetWidth;
        const menuH = msgMenu.offsetHeight;

        let left = rect.left;
        let top = rect.bottom + 6;
        if (left + menuW > window.innerWidth - 8) left = window.innerWidth - menuW - 8;
        if (left < 8) left = 8;
        if (top + menuH > window.innerHeight - 8) top = rect.top - menuH - 6;
        if (top < 8) top = 8;

        msgMenu.style.left = left + 'px';
        msgMenu.style.top = top + 'px';
    }

    function closeMsgMenu() {
        msgMenu.classList.remove('open');
        if (menuActiveEl) {
            menuActiveEl.classList.remove('menu-active');
            menuActiveEl = null;
        }
    }

    document.getElementById('msgMenuReply').onclick = function() {
        if (menuTarget) startReply(menuTarget.messageId, menuTarget.senderName, menuTarget.text, menuTarget.mediaUrls);
        closeMsgMenu();
    };
    document.getElementById('msgMenuForward').onclick = function() {
        if (menuTarget) openForwardModal(menuTarget);
        closeMsgMenu();
    };
    document.getElementById('msgMenuRemoveMe').onclick = function() {
        if (menuTarget) removeMessageForMe(menuTarget.messageId);
        closeMsgMenu();
    };
    document.getElementById('msgMenuRemoveEveryone').onclick = function() {
        const id = menuTarget && menuTarget.messageId;
        closeMsgMenu();
        if (id == null) return;
        showDialog({
            title: 'Remove for everyone?',
            message: 'This message will be removed for everyone in this chat. This can’t be undone.',
            confirmText: 'Remove',
            cancelText: 'Cancel',
            danger: true,
            icon: 'warn',
            onConfirm: () => removeMessageForEveryone(id)
        });
    };

    // ===== Remove message =====
    // Builds the "This message was deleted" tombstone bubble
    function buildTombstone() {
        const content = document.createElement('div');
        content.className = 'message-content deleted';
        content.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line></svg><span>This message was deleted</span>';
        return content;
    }

    // Convert an on-screen message into a tombstone in place (idempotent)
    function applyTombstone(el) {
        if (!el) return;
        const stack = el.querySelector('.message-stack');
        if (!stack) return;
        stack.querySelectorAll('.message-content, .media-container, .msg-menu-btn').forEach(n => n.remove());
        stack.insertBefore(buildTombstone(), stack.firstChild);
        attachKebab(stack, { messageId: el.dataset.messageId, deleted: true }, el.classList.contains('message-sent'));
    }

    // Remove for me only: hide on my side; the row stays for the other person
    function removeMessageForMe(messageId) {
        if (messageId == null) return;
        authFetch(`/v1/api/message/${messageId}/me`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const el = chatMessages.querySelector(`[data-message-id="${messageId}"]`);
                    if (el) el.remove();
                } else {
                    showDialog({ title: 'Could not remove', message: data.message || 'Please try again.', icon: 'warn' });
                }
            })
            .catch(() => {});
    }

    // Remove for everyone: tombstone for both participants (sender only)
    function removeMessageForEveryone(messageId) {
        if (messageId == null) return;
        authFetch(`/v1/api/message/${messageId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    applyTombstone(chatMessages.querySelector(`[data-message-id="${messageId}"]`));
                } else {
                    showDialog({ title: 'Could not remove', message: data.message || 'Please try again.', icon: 'warn' });
                }
            })
            .catch(() => {});
    }

    // Dismiss the popup on outside click, scroll, resize or Escape
    document.addEventListener('click', function(e) {
        if (msgMenu.classList.contains('open') && !msgMenu.contains(e.target)) closeMsgMenu();
    });
    window.addEventListener('resize', closeMsgMenu);
    chatMessages.addEventListener('scroll', closeMsgMenu);
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') { closeMsgMenu(); closeForwardModal(); closeDialog(); closeSettings(); }
    });
    forwardModal.addEventListener('click', function(e) {
        if (e.target === forwardModal) closeForwardModal(); // click on backdrop
    });

    // ===== Forward =====
    function openForwardModal(target) {
        menuTarget = target; // retain target for the forward action
        forwardPreview.textContent = buildForwardPreview(target);
        renderForwardList();
        forwardModal.classList.add('open');
    }

    function closeForwardModal() {
        forwardModal.classList.remove('open');
    }

    function buildForwardPreview(target) {
        if (target.text && target.text.trim() !== '') {
            const t = target.text.trim();
            return 'Forwarding: ' + (t.length > 60 ? t.slice(0, 60) + '…' : t);
        }
        if (target.mediaUrls && target.mediaUrls.length > 0) return 'Forwarding: 📷 Media';
        return 'Forwarding message';
    }

    function renderForwardList() {
        forwardList.innerHTML = '';
        const others = contactsCache.filter(u => u.userId != currentUser.id);
        if (others.length === 0) {
            forwardList.innerHTML = '<div class="forward-empty">No contacts available</div>';
            return;
        }
        others.forEach(user => {
            const item = document.createElement('div');
            item.className = 'forward-item';

            const avatar = document.createElement('div');
            avatar.className = 'user-avatar';
            fillAvatar(avatar, user);

            const name = document.createElement('div');
            name.className = 'forward-item-name';
            name.textContent = displayName(user);

            item.appendChild(avatar);
            item.appendChild(name);
            item.onclick = () => forwardMessage(user);
            forwardList.appendChild(item);
        });
    }

    function forwardMessage(user) {
        if (!menuTarget) return;
        if (!connected || !stompClient) {
            showDialog({ title: 'Not connected', message: 'You are not connected. Please try again in a moment.', icon: 'warn' });
            return;
        }

        const chatMessage = {
            recipientId: user.userId,
            message: menuTarget.text || '',
            senderVisibility: 1,
            recipientVisibility: 1,
            mediaUrls: menuTarget.mediaUrls || [],
            replyToMessageId: null,
            clientId: newClientId() // correlate the optimistic bubble with the server echo
        };

        const headers = { 'Authorization': 'Bearer ' + currentUser.token };
        stompClient.send("/app/chat", headers, JSON.stringify(chatMessage));

        // If forwarding into the conversation that's currently open, echo it locally
        if (activeRecipientId == user.userId) {
            showMessage({ ...chatMessage, senderId: currentUser.id, isRead: false }, true);
        }

        closeForwardModal();
    }

    function loadChatHistory(recipientId) {
        chatMessages.innerHTML = '<div class="messages-loading"><span class="spinner"></span>Loading messages…</div>';
        authFetch(`/v1/api/message/${recipientId}?size=50`, { method: 'GET' })
        .then(res => res.json())
        .then(data => {
            // Ignore a stale response if the user switched conversations meanwhile
            if (activeRecipientId != recipientId) return;
            chatMessages.innerHTML = '';
            if (data.code === 200 && data.data.content) {
                const messages = [...data.data.content].reverse();
                messages.forEach(msg => {
                    const isSender = msg.senderId == currentUser.id;
                    showMessage(msg, isSender);
                });
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }
        })
        .catch(err => {
            if (activeRecipientId == recipientId) chatMessages.innerHTML = '';
            console.error("Failed to load history:", err);
        });
    }

    function updateConnectionStatus(isConnected) {
        connected = isConnected;
        if (isConnected) {
            connectionIndicator.classList.add('status-connected');
            connectionText.textContent = 'Connected';
        } else {
            connectionIndicator.classList.remove('status-connected');
            connectionText.textContent = 'Disconnected';
        }
        updateSendButtonState();
    }

    function updateSendButtonState() {
        const hasContent = messageInput.value.trim() !== '' || selectedFiles.length > 0;
        const canSend = connected && activeRecipientId != null && hasContent;
        sendButton.disabled = !canSend;
    }

    // Connect to WebSocket
    function connect() {
        if (!currentUser) return;
        if (stompClient !== null) stompClient.disconnect();

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        const headers = { 'Authorization': 'Bearer ' + currentUser.token };

        stompClient.connect(headers, function (frame) {
            updateConnectionStatus(true);

            // Subscribe to private messages
            stompClient.subscribe(`/user/${currentUser.id}/queue/messages`, function (message) {
                const messageData = JSON.parse(message.body);
                if (messageData.senderId == currentUser.id) {
                    // Echo of my own message — reconcile the optimistic bubble with its real id
                    reconcileOwnMessage(messageData);
                } else {
                    // Refresh that contact's preview even if their chat isn't open
                    updateContactPreview(messageData.senderId, messageData, false);
                    if (messageData.senderId == activeRecipientId) {
                        showMessage(messageData, false);
                        sendReadReceipt(activeRecipientId);
                    }
                }
            });

            // Subscribe to typing notifications
            stompClient.subscribe(`/user/${currentUser.id}/queue/typing`, function (message) {
                const typingData = JSON.parse(message.body);
                if (typingData.senderId == activeRecipientId) {
                    if (typingData.isTyping) showTypingBubble();
                    else hideTypingBubble();
                }
            });

            // Subscribe to read receipts
            stompClient.subscribe(`/user/${currentUser.id}/queue/read-receipt`, function (message) {
                const readData = JSON.parse(message.body);
                if (readData.readBy == activeRecipientId) {
                    updateMessagesToRead();
                }
            });

            // Subscribe to "removed for everyone" deletions
            stompClient.subscribe(`/user/${currentUser.id}/queue/message-deleted`, function (message) {
                const data = JSON.parse(message.body);
                const el = chatMessages.querySelector(`[data-message-id="${data.messageId}"]`);
                if (el) applyTombstone(el);
            });

            // Subscribe to account status changes (admin locked/suspended this account)
            stompClient.subscribe(`/user/${currentUser.id}/queue/account`, function (message) {
                handleAccountStatus(JSON.parse(message.body));
            });

            // Subscribe to public status updates
            stompClient.subscribe('/topic/public.status', function (message) {
                const statusData = JSON.parse(message.body);
                updateUserStatus(statusData.userId, statusData.status === 'ONLINE');
            });

        }, function(error) {
            console.error('Connection error:', error);
            updateConnectionStatus(false);
            if (currentUser) setTimeout(connect, 5000);
        });
    }

    function updateUserStatus(userId, isOnline) {
        if (isOnline) onlineUsers.add(String(userId));
        else onlineUsers.delete(String(userId));

        const dot = document.getElementById(`dot-${userId}`);

        if (dot) {
            if (isOnline) dot.classList.add('online');
            else dot.classList.remove('online');
        }

        // The contact list shows the latest message under each name, not presence —
        // the dot already conveys online/offline there.

        // Update header if chatting with this user
        if (activeRecipientId == userId) {
            const headerStatus = activeChatHeader.querySelector('div div:last-child');
            if (headerStatus) {
                headerStatus.textContent = isOnline ? 'Online' : 'Offline';
                headerStatus.style.color = isOnline ? 'var(--success-color)' : '#666';
            }
        }
    }

    function handleTyping() {
        updateSendButtonState();
        if (!connected || !activeRecipientId) return;

        stompClient.send("/app/chat.typing", {}, JSON.stringify({
            recipientId: activeRecipientId,
            username: currentUser.username,
            isTyping: true
        }));

        if (isTypingTimeout) clearTimeout(isTypingTimeout);
        isTypingTimeout = setTimeout(() => {
            stompClient.send("/app/chat.typing", {}, JSON.stringify({
                recipientId: activeRecipientId,
                username: currentUser.username,
                isTyping: false
            }));
        }, 2000);
    }

    function sendReadReceipt(recipientId) {
        if (!connected || !recipientId) return;
        stompClient.send("/app/chat.read", {}, JSON.stringify({
            recipientId: recipientId
        }));
    }

    function updateMessagesToRead() {
        const unreadSentMessages = document.querySelectorAll('.message-sent .read-status');
        unreadSentMessages.forEach(el => {
            el.textContent = 'Read';
        });
    }

    // Unique id for correlating an optimistic bubble with its server echo
    function newClientId() {
        return 'c-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
    }

    // Send message
    function sendMessage() {
        if (!connected || !currentUser || !activeRecipientId) return;

        const hasContent = messageInput.value.trim() !== '' || selectedFiles.length > 0;
        if (!hasContent) return;

        const chatMessage = {
            recipientId: activeRecipientId,
            message: messageInput.value,
            senderVisibility: 1,
            recipientVisibility: 1,
            mediaUrls: [],
            replyToMessageId: replyingTo ? replyingTo.messageId : null,
            clientId: newClientId() // correlate the optimistic bubble with the server echo
        };

        // Snapshot reply info for optimistic rendering (resetInput clears replyingTo)
        const replySnapshot = replyingTo ? {
            replyToSenderUsername: replyingTo.senderName,
            replyToSnippet: replyingTo.snippet
        } : {};

        const headers = { 'Authorization': 'Bearer ' + currentUser.token };

        if (selectedFiles.length > 0) {
            const formData = new FormData();
            for (const file of selectedFiles) formData.append("files", file);

            uploadMediaFiles(formData).then(mediaUrls => {
                chatMessage.mediaUrls = mediaUrls;
                stompClient.send("/app/chat", headers, JSON.stringify(chatMessage));
                showMessage({...chatMessage, ...replySnapshot, senderId: currentUser.id, isRead: false}, true);
                resetInput();
            }).catch(error => {
                console.error("Error uploading files:", error);
                showDialog({ title: 'Upload failed', message: 'Failed to upload media files. Please try again.', icon: 'warn' });
            });
        } else {
            stompClient.send("/app/chat", headers, JSON.stringify(chatMessage));
            showMessage({...chatMessage, ...replySnapshot, senderId: currentUser.id, isRead: false}, true);
            resetInput();
        }
    }

    function resetInput() {
        messageInput.value = '';
        clearFileSelection();
        cancelReply();
        updateSendButtonState();

        if (isTypingTimeout) clearTimeout(isTypingTimeout);
        stompClient.send("/app/chat.typing", {}, JSON.stringify({
            recipientId: activeRecipientId,
            username: currentUser.username,
            isTyping: false
        }));
    }

    function uploadMediaFiles(formData) {
        return new Promise((resolve, reject) => {
            authFetch('/v1/api/media/chat', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (!response.ok) throw new Error("Failed to upload media files");
                return response.json();
            })
            .then(data => resolve(data))
            .catch(error => reject(error));
        });
    }

    // Show message in chat
    function showMessage(messageData, isSender = false) {
        const messageElement = document.createElement('div');
        messageElement.className = isSender ? 'message message-sent' : 'message message-received';
        if (isSender && messageData.clientId) messageElement.dataset.clientId = messageData.clientId;

        const infoElement = document.createElement('div');
        infoElement.className = 'message-info';

        let timeString = "";
        const timestamp = messageData.sentAt || messageData.createdAt;
        if (timestamp) {
            const date = new Date(timestamp);
            timeString = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        } else {
            const now = new Date();
            timeString = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
        }

        infoElement.textContent = isSender ? `You • ${timeString}` : `${timeString}`;

        if (isSender) {
            const readStatus = document.createElement('span');
            readStatus.className = 'read-status';
            readStatus.textContent = messageData.isRead ? 'Read' : 'Sent';
            infoElement.appendChild(readStatus);
        }

        messageElement.appendChild(infoElement);

        // Bubble + media live in a stack so the kebab can center on them (not the info line)
        const stack = document.createElement('div');
        stack.className = 'message-stack';

        const isDeleted = messageData.deleted === true || messageData.deleted === 1;
        if (isDeleted) stack.appendChild(buildTombstone());

        const hasText = !isDeleted && messageData.message && messageData.message.trim() !== '';
        const hasReply = !isDeleted && messageData.replyToMessageId != null &&
            (messageData.replyToSnippet != null || messageData.replyToSenderUsername != null);

        if (hasText || hasReply) {
            const contentElement = document.createElement('div');
            contentElement.className = 'message-content';

            if (hasReply) {
                const quote = document.createElement('div');
                quote.className = 'reply-quote';

                const quoteSender = document.createElement('div');
                quoteSender.className = 'reply-quote-sender';
                quoteSender.textContent = messageData.replyToSenderUsername || 'Message';

                const quoteText = document.createElement('div');
                quoteText.className = 'reply-quote-text';
                quoteText.textContent = messageData.replyToSnippet || '';

                quote.appendChild(quoteSender);
                quote.appendChild(quoteText);
                contentElement.appendChild(quote);
            }

            if (hasText) {
                const textEl = document.createElement('div');
                textEl.className = 'message-text';
                textEl.textContent = messageData.message;
                contentElement.appendChild(textEl);
            }

            stack.appendChild(contentElement);
        }

        if (!isDeleted && messageData.mediaUrls && messageData.mediaUrls.length > 0) {
            const mediaContainer = document.createElement('div');
            mediaContainer.className = 'media-container';

            for (const url of messageData.mediaUrls) {
                if (url.match(/\.(jpeg|jpg|gif|png|webp)$/i)) {
                    const img = document.createElement('img');
                    img.src = url;
                    img.onclick = () => window.open(url, '_blank');
                    mediaContainer.appendChild(img);
                } else if (url.match(/\.(mp4|webm|ogg)$/i)) {
                    const video = document.createElement('video');
                    video.src = url;
                    video.controls = true;
                    mediaContainer.appendChild(video);
                }
            }
            stack.appendChild(mediaContainer);
        }

        const msgId = messageData.messageId != null ? messageData.messageId : messageData.id;
        if (msgId != null) messageElement.dataset.messageId = msgId;

        // Message actions (kebab) — needs a persisted message id to act on
        attachKebab(stack, messageData, isSender);

        messageElement.appendChild(stack);
        chatMessages.appendChild(messageElement);
        // Keep the typing bubble (if visible) as the last item in the list
        if (typingBubbleEl && typingBubbleEl.parentNode === chatMessages) {
            chatMessages.appendChild(typingBubbleEl);
        }
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return messageElement;
    }

    // Add the kebab (Reply/Forward) button to a message stack. Requires a persisted
    // message id, so optimistic bubbles get it later via reconcileOwnMessage().
    function attachKebab(stack, messageData, isSender) {
        const msgId = messageData.messageId != null ? messageData.messageId : messageData.id;
        if (msgId == null) return;
        if (stack.querySelector('.msg-menu-btn')) return; // already has one

        const menuBtn = document.createElement('button');
        menuBtn.className = 'msg-menu-btn';
        menuBtn.title = 'Message actions';
        menuBtn.setAttribute('aria-label', 'Message actions');
        menuBtn.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="1.6"></circle><circle cx="12" cy="12" r="1.6"></circle><circle cx="12" cy="19" r="1.6"></circle></svg>';
        const senderName = isSender ? 'You' : (messageData.senderUsername || activeContactUsername || 'Them');
        const target = {
            messageId: msgId,
            senderName: senderName,
            text: messageData.message,
            mediaUrls: messageData.mediaUrls,
            isSender: isSender,
            deleted: messageData.deleted === true || messageData.deleted === 1
        };
        menuBtn.onclick = (e) => openMsgMenu(e, target, menuBtn);
        stack.appendChild(menuBtn);
    }

    // When my own message comes back from the server (with a real id), upgrade the
    // optimistic bubble in place — attaching the kebab — instead of duplicating it.
    function reconcileOwnMessage(messageData) {
        const realId = messageData.id != null ? messageData.id : messageData.messageId;

        // Reflect my latest message in the recipient's contact-list preview
        updateContactPreview(messageData.recipientId, messageData, true);

        if (messageData.clientId) {
            const existing = chatMessages.querySelector(`[data-client-id="${messageData.clientId}"]`);
            if (existing) {
                const stack = existing.querySelector('.message-stack');
                if (stack) attachKebab(stack, messageData, true);
                if (realId != null) existing.dataset.messageId = realId;
                delete existing.dataset.clientId; // reconciled
                return;
            }
        }
        // No optimistic match (sent from another tab, or after switching away and back
        // so history already rendered it) — render only if it's not already on screen.
        if (realId != null && chatMessages.querySelector(`[data-message-id="${realId}"]`)) return;
        if (messageData.recipientId == activeRecipientId) showMessage(messageData, true);
    }

    // Handle file selection (triggered by the <input change> event)
    function handleFileSelection() {
        selectedFiles = Array.from(mediaFilesInput.files);
        renderFilePreviews();
    }

    // Render previews from the current selectedFiles array (decoupled from the input)
    function renderFilePreviews() {
        fileCount.textContent = selectedFiles.length;
        fileCount.style.display = selectedFiles.length > 0 ? 'flex' : 'none';
        filePreviewContainer.innerHTML = '';

        selectedFiles.forEach((file, index) => {
            const previewItem = document.createElement('div');
            previewItem.className = 'preview-item';

            if (file.type.startsWith('image/')) {
                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                previewItem.appendChild(img);
            } else {
                const img = document.createElement('img');
                img.src = 'https://via.placeholder.com/80?text=File';
                previewItem.appendChild(img);
            }

            const removeButton = document.createElement('button');
            removeButton.className = 'preview-remove';
            removeButton.innerHTML = '×';
            removeButton.onclick = (e) => {
                e.preventDefault();
                removeFile(index);
            };

            previewItem.appendChild(removeButton);
            filePreviewContainer.appendChild(previewItem);
        });

        filePreviewContainer.style.display = selectedFiles.length > 0 ? 'flex' : 'none';
        updateSendButtonState();
    }

    function removeFile(index) {
        selectedFiles.splice(index, 1);
        renderFilePreviews();
    }

    function clearFileSelection() {
        mediaFilesInput.value = '';
        selectedFiles = [];
        fileCount.textContent = '0';
        fileCount.style.display = 'none';
        filePreviewContainer.innerHTML = '';
        filePreviewContainer.style.display = 'none';
        updateSendButtonState();
    }

    // Event listeners
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !sendButton.disabled) {
            sendMessage();
        }
    });
    mediaFilesInput.addEventListener('change', handleFileSelection);

    // Submit auth forms on Enter
    ['username', 'password'].forEach(id => {
        document.getElementById(id).addEventListener('keypress', e => { if (e.key === 'Enter') login(); });
    });
    ['suFirstName', 'suLastName', 'suEmail', 'suUsername', 'suPhone', 'suPassword', 'suConfirm'].forEach(id => {
        document.getElementById(id).addEventListener('keypress', e => { if (e.key === 'Enter') signup(); });
    });
    loadPhoneCodes();
