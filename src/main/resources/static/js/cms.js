// ===== Modern Chat — Admin CMS =====
// Standalone admin site: its own login, its own session (localStorage 'cms_session'),
// and its own styling (cms.css). Independent of the chat app.

const CMS_SESSION_KEY = 'cms_session';
let currentUser = JSON.parse(localStorage.getItem(CMS_SESSION_KEY) || 'null');

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const USER_PAGE_SIZE = 10;

let allUsers = [];
let userPage = 0;
let roles = [];
let editingUserId = null;
let editingRoleId = null;
let userModalInitial = null;
let roleModalInitial = null;

// ===== Small helpers =====
function esc(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function val(id) { return document.getElementById(id).value.trim(); }
function setError(id, msg) {
    const el = document.getElementById(id);
    el.textContent = msg || '';
    el.style.display = msg ? 'block' : 'none';
}
function btnLoading(btn, loading, idle) {
    if (loading) {
        btn.dataset.idle = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="btn-loading"><span class="spinner"></span>Saving…</span>';
    } else {
        btn.disabled = false;
        btn.innerHTML = idle != null ? idle : (btn.dataset.idle || 'Save');
    }
}
function displayName(u) {
    if (!u) return 'Unknown';
    const full = [u.firstName, u.lastName].filter(s => s && s.trim()).join(' ').trim();
    return full || u.username || 'Unknown';
}
function roleLabel(r) {
    if (!r) return '—';
    const name = r.replace(/^ROLE_/, '').toLowerCase();
    return name.charAt(0).toUpperCase() + name.slice(1);
}
function statusValue(s) {
    const v = (s || '').toLowerCase();
    if (v === 'active') return 1;
    if (v === 'inactive') return 2;
    if (v === 'suspended') return 3;
    return 1;
}
function avatarHtml(u, cls) {
    if (u.profilePicture) {
        return `<div class="${cls} has-image" style="background-image:url('${esc(u.profilePicture)}')"></div>`;
    }
    return `<div class="${cls}">${esc((displayName(u) || '?').charAt(0).toUpperCase())}</div>`;
}

// ===== Theme =====
// persist=false applies a theme without remembering it (used for the OS default,
// so an untouched session keeps following the machine theme).
function applyTheme(theme, persist = true) {
    const dark = theme === 'dark';
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    if (persist) localStorage.setItem('chat_theme', dark ? 'dark' : 'light');
}
function prefersDarkOS() {
    return !!(window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches);
}
// Stored choice wins; otherwise follow the OS theme (and keep following it live
// until the user makes their own choice).
function initTheme() {
    const stored = localStorage.getItem('chat_theme');
    applyTheme(stored || (prefersDarkOS() ? 'dark' : 'light'), false);
    if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            if (!localStorage.getItem('chat_theme')) applyTheme(e.matches ? 'dark' : 'light', false);
        });
    }
}
function toggleTheme() {
    applyTheme(document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark');
}

// ===== Styled dialog (alert / confirm) =====
const appDialog = document.getElementById('appDialog');
const DIALOG_ICONS = {
    info: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>',
    warn: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>'
};
function showDialog({ title = 'Notice', message = '', confirmText = 'OK', cancelText = null, danger = false, icon = 'info', onConfirm = null } = {}) {
    const iconEl = document.getElementById('appDialogIcon');
    iconEl.className = 'dialog-icon' + (danger || icon === 'warn' ? ' warn' : '');
    iconEl.innerHTML = DIALOG_ICONS[danger ? 'warn' : icon] || DIALOG_ICONS.info;
    document.getElementById('appDialogTitle').textContent = title;
    document.getElementById('appDialogMessage').innerHTML = message;
    const actions = document.getElementById('appDialogActions');
    actions.innerHTML = '';
    if (cancelText) {
        const c = document.createElement('button');
        c.className = 'dialog-btn';
        c.textContent = cancelText;
        c.onclick = closeDialog;
        actions.appendChild(c);
    }
    const ok = document.createElement('button');
    ok.className = 'dialog-btn primary' + (danger ? ' danger' : '');
    ok.textContent = confirmText;
    ok.onclick = () => { closeDialog(); if (onConfirm) onConfirm(); };
    actions.appendChild(ok);
    appDialog.classList.add('open');
}
function closeDialog() { appDialog.classList.remove('open'); }
appDialog.addEventListener('click', e => { if (e.target === appDialog) closeDialog(); });

// ===== Auth =====
let refreshPromise = null;

// Exchange the stored refresh token for a fresh access/refresh pair.
function refreshAccessToken() {
    if (refreshPromise) return refreshPromise;
    if (!currentUser || !currentUser.refreshToken) return Promise.resolve(false);
    refreshPromise = fetch('/v1/api/auth/refresh-token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: currentUser.refreshToken })
    })
    .then(r => r.json())
    .then(data => {
        if (data.code === 200 && data.data && data.data.token) {
            currentUser = data.data;
            localStorage.setItem(CMS_SESSION_KEY, JSON.stringify(currentUser));
            return true;
        }
        return false;
    })
    .catch(() => false)
    .finally(() => { refreshPromise = null; });
    return refreshPromise;
}

// Session no longer valid — drop it and return to the admin login screen.
function expireSession() {
    localStorage.removeItem(CMS_SESSION_KEY);
    currentUser = null;
    showLogin('Your session has expired. Please sign in again.');
}

function authFetch(url, opts = {}, retried = false) {
    const o = { ...opts };
    o.headers = { ...(opts.headers || {}), 'Authorization': 'Bearer ' + currentUser.token };
    return fetch(url, o).then(r => {
        if (r.status !== 401) return r;
        if (retried) { expireSession(); throw new Error('Unauthorized'); }
        return refreshAccessToken().then(ok => {
            if (ok) return authFetch(url, opts, true);
            expireSession();
            throw new Error('Unauthorized');
        });
    });
}

function showLogin(message) {
    document.getElementById('cmsApp').style.display = 'none';
    document.getElementById('cmsLogin').style.display = 'flex';
    setError('cmsLoginError', message || '');
    const u = document.getElementById('cmsUsername');
    if (u) u.focus();
}

function showApp() {
    document.getElementById('cmsLogin').style.display = 'none';
    document.getElementById('cmsApp').style.display = 'block';
    renderAdminIdentity();
    loadRoles().then(loadUsers);
}

function togglePassword(btn) {
    const input = btn.parentElement.querySelector('input');
    input.type = input.type === 'password' ? 'text' : 'password';
}

// Authenticate against the shared sign-in API, but only admins may enter the CMS.
function adminLogin() {
    const username = val('cmsUsername');
    const password = document.getElementById('cmsPassword').value;
    if (!username || !password) return setError('cmsLoginError', 'Please enter your username and password.');
    setError('cmsLoginError', '');

    const btn = document.getElementById('cmsLoginBtn');
    btnLoading(btn, true);
    fetch('/v1/api/auth/sign-in', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
        .then(r => r.json())
        .then(data => {
            if ((data.code === 200 || data.code === 201) && data.data && data.data.token) {
                if (data.data.role !== 'ROLE_ADMIN') {
                    setError('cmsLoginError', 'This account does not have administrator access.');
                    return;
                }
                currentUser = data.data;
                localStorage.setItem(CMS_SESSION_KEY, JSON.stringify(currentUser));
                document.getElementById('cmsPassword').value = '';
                showApp();
            } else {
                setError('cmsLoginError', data.message || 'Invalid username or password.');
            }
        })
        .catch(() => setError('cmsLoginError', 'Something went wrong. Please try again.'))
        .finally(() => btnLoading(btn, false, 'Sign in'));
}

// Revoke the refresh token, drop the session, and return to login.
function logout() {
    const token = currentUser && currentUser.refreshToken;
    const done = () => {
        localStorage.removeItem(CMS_SESSION_KEY);
        currentUser = null;
        showLogin();
    };
    if (!token) return done();
    fetch('/v1/api/auth/logout', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: token })
    }).catch(() => {}).finally(done);
}

function renderAdminIdentity() {
    document.getElementById('cmsAdminName').textContent = displayName(currentUser);
    const av = document.getElementById('cmsAdminAvatar');
    if (currentUser.profilePicture) {
        av.classList.add('has-image');
        av.style.backgroundImage = `url("${currentUser.profilePicture}")`;
    } else {
        av.textContent = (displayName(currentUser) || '?').charAt(0).toUpperCase();
    }
}

// ===== Tabs =====
function switchTab(tab) {
    document.getElementById('tab-users').classList.toggle('active', tab === 'users');
    document.getElementById('tab-roles').classList.toggle('active', tab === 'roles');
    document.getElementById('panel-users').style.display = tab === 'users' ? 'block' : 'none';
    document.getElementById('panel-roles').style.display = tab === 'roles' ? 'block' : 'none';
}

// ===== Users =====
function loadUsers() {
    return authFetch('/v1/api/users?page=0&size=1000')
        .then(r => r.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                allUsers = data.data.content || [];
                renderUsers();
            }
        })
        .catch(() => {});
}

function statusBadgeHtml(s) {
    const v = (s || '').toLowerCase();
    let cls = 'status-inactive';
    if (v === 'active') cls = 'status-active';
    else if (v === 'suspended') cls = 'status-suspended';
    return `<span class="cms-badge ${cls}">${esc(s || 'Unknown')}</span>`;
}

function userRowHtml(u) {
    const roleBadge = `<span class="cms-badge ${u.role === 'ROLE_ADMIN' ? 'role-admin' : ''}">${esc(roleLabel(u.role))}</span>`;
    const isSelf = currentUser && u.userId === currentUser.id;
    const deleteBtn = isSelf
        ? ''
        : `<button class="cms-row-btn danger" onclick="confirmDeleteUser(${u.userId})">Delete</button>`;
    return `<tr>
        <td><div class="cms-user-cell">${avatarHtml(u, 'cms-user-avatar')}<span class="cms-user-name">${esc(displayName(u))}${isSelf ? ' <span class="cms-muted">(you)</span>' : ''}</span></div></td>
        <td>${esc(u.username || '')}</td>
        <td class="cms-muted">${esc(u.email || '')}</td>
        <td>${roleBadge}</td>
        <td>${statusBadgeHtml(u.status)}</td>
        <td class="cms-muted">${esc(u.createdAt || '')}</td>
        <td class="cms-col-actions">
            <button class="cms-row-btn" onclick="editUser(${u.userId})">Edit</button>
            ${deleteBtn}
        </td>
    </tr>`;
}

function renderUsers() {
    const q = document.getElementById('userSearch').value.trim().toLowerCase();
    let list = allUsers;
    if (q) {
        list = allUsers.filter(u =>
            (displayName(u) + ' ' + (u.username || '') + ' ' + (u.email || '')).toLowerCase().includes(q));
    }
    const totalPages = Math.max(1, Math.ceil(list.length / USER_PAGE_SIZE));
    if (userPage >= totalPages) userPage = totalPages - 1;
    if (userPage < 0) userPage = 0;
    const pageItems = list.slice(userPage * USER_PAGE_SIZE, userPage * USER_PAGE_SIZE + USER_PAGE_SIZE);

    document.getElementById('usersTbody').innerHTML = pageItems.map(userRowHtml).join('');
    document.getElementById('usersEmpty').style.display = list.length ? 'none' : 'block';

    const pg = document.getElementById('usersPagination');
    if (!list.length) { pg.innerHTML = ''; return; }
    pg.innerHTML = `
        <span>${list.length} user${list.length === 1 ? '' : 's'} · Page ${userPage + 1} of ${totalPages}</span>
        <button class="cms-page-btn" ${userPage === 0 ? 'disabled' : ''} onclick="changeUserPage(-1)">Prev</button>
        <button class="cms-page-btn" ${userPage >= totalPages - 1 ? 'disabled' : ''} onclick="changeUserPage(1)">Next</button>`;
}

function changeUserPage(d) { userPage += d; renderUsers(); }

function populateRoleSelect(selectedRoleName) {
    const sel = document.getElementById('uRole');
    sel.innerHTML = roles.map(r => `<option value="${r.roleId}">${esc(roleLabel(r.roleName))}</option>`).join('');
    const match = roles.find(r => r.roleName === selectedRoleName);
    if (match) sel.value = String(match.roleId);
}

function openUserModal(user) {
    editingUserId = user ? user.userId : null;
    document.getElementById('userModalTitle').textContent = user ? 'Edit user' : 'Add user';
    document.getElementById('uFirstName').value = user ? (user.firstName || '') : '';
    document.getElementById('uLastName').value = user ? (user.lastName || '') : '';
    document.getElementById('uEmail').value = user ? (user.email || '') : '';
    document.getElementById('uUsername').value = user ? (user.username || '') : '';
    document.getElementById('uPassword').value = '';
    document.getElementById('uPasswordLabel').textContent = user ? 'New password' : 'Password';
    document.getElementById('uPassword').placeholder = user ? 'Leave blank to keep' : 'Password';
    document.getElementById('uPasswordHint').style.display = user ? 'block' : 'none';
    populateRoleSelect(user ? user.role : 'ROLE_USER');
    document.getElementById('uStatusGroup').style.display = user ? 'block' : 'none';
    if (user) document.getElementById('uStatus').value = String(statusValue(user.status));
    document.getElementById('uStatusReason').value = '';
    toggleStatusReason();
    setError('userModalError', '');
    userModalInitial = userFormState();
    updateUserSaveState();
    document.getElementById('userModal').classList.add('open');
}
function closeUserModal() { document.getElementById('userModal').classList.remove('open'); }

// Reason field only matters when editing AND moving the account to a non-active status
function toggleStatusReason() {
    const locked = editingUserId && document.getElementById('uStatus').value !== '1';
    document.getElementById('uStatusReasonGroup').style.display = locked ? 'block' : 'none';
}

// Serialize the editable form fields so we can tell whether anything changed.
function userFormState() {
    return JSON.stringify({
        firstName: val('uFirstName'),
        lastName: val('uLastName'),
        email: val('uEmail'),
        username: val('uUsername'),
        password: document.getElementById('uPassword').value,
        roleId: document.getElementById('uRole').value,
        status: document.getElementById('uStatus').value,
        statusReason: document.getElementById('uStatusReason').value.trim()
    });
}
// When editing, keep Save disabled until the admin actually changes something.
// Creating a new user has no baseline, so Save stays enabled.
function updateUserSaveState() {
    const btn = document.getElementById('userModalSave');
    btn.disabled = editingUserId ? (userFormState() === userModalInitial) : false;
}
function editUser(id) { const u = allUsers.find(x => x.userId === id); if (u) openUserModal(u); }

function saveUser() {
    const firstName = val('uFirstName'), lastName = val('uLastName'), email = val('uEmail'), username = val('uUsername');
    const password = document.getElementById('uPassword').value;
    const roleId = Number(document.getElementById('uRole').value) || null;

    setError('userModalError', '');

    const btn = document.getElementById('userModalSave');
    btnLoading(btn, true);

    let req;
    if (editingUserId) {
        const statusVal = Number(document.getElementById('uStatus').value);
        const body = { firstName, lastName, email, username, roleId, status: statusVal };
        if (password) body.password = password;
        if (statusVal !== 1) body.statusReason = document.getElementById('uStatusReason').value.trim();
        req = authFetch('/v1/api/users/' + editingUserId, {
            method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
        });
    } else {
        req = authFetch('/v1/api/users', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ firstName, lastName, email, username, password, roleId })
        });
    }

    req.then(r => r.json())
        .then(data => {
            if (data.code === 200 || data.code === 201) {
                // If the admin just stripped their own admin role, they can no longer
                // use the CMS — end the session and return to the login screen.
                const newRole = (roles.find(r => r.roleId === roleId) || {}).roleName;
                if (editingUserId && currentUser && editingUserId === currentUser.id
                        && newRole && newRole !== 'ROLE_ADMIN') {
                    closeUserModal();
                    const rt = currentUser.refreshToken;
                    localStorage.removeItem(CMS_SESSION_KEY);
                    currentUser = null;
                    if (rt) fetch('/v1/api/auth/logout', {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ refreshToken: rt })
                    }).catch(() => {});
                    // Lock the page down immediately so dismissing the dialog can't
                    // leave them in a CMS they no longer have rights to use.
                    showLogin();
                    showDialog({
                        title: 'Admin access removed',
                        message: 'You changed your own role, so you no longer have administrator access. You have been signed out.',
                        confirmText: 'OK', icon: 'warn'
                    });
                    return;
                }
                closeUserModal();
                loadUsers();
            } else {
                setError('userModalError', data.message || 'Could not save the user.');
            }
        })
        .catch(() => setError('userModalError', 'Something went wrong. Please try again.'))
        .finally(() => btnLoading(btn, false, 'Save'));
}

function confirmDeleteUser(id) {
    const u = allUsers.find(x => x.userId === id);
    if (!u) return;
    
    const messageHtml = `
        <div style="margin-bottom: 8px;">Delete "<b>${esc(displayName(u))}</b>" (@${esc(u.username)})? They will be removed from the app.</div>
        <div class="cms-toggle-wrap" style="text-align: left;">
            <div class="cms-toggle-info">
                <span class="cms-toggle-label">Hard Delete</span>
                <span class="cms-toggle-desc" id="deleteTypeDesc">Turn on to permanently wipe this user's data from the database.</span>
            </div>
            <label class="cms-toggle">
                <input type="checkbox" id="hardDeleteToggle" onchange="document.getElementById('deleteTypeDesc').innerText = this.checked ? 'User and data will be permanently wiped.' : 'Turn on to permanently wipe this user\\'s data from the database.'">
                <span class="cms-toggle-slider"></span>
            </label>
        </div>
    `;

    showDialog({
        title: 'Delete user',
        message: messageHtml,
        confirmText: 'Delete', cancelText: 'Cancel', danger: true,
        onConfirm: () => {
            const isHardDelete = document.getElementById('hardDeleteToggle').checked ? 1 : 0;
            authFetch('/v1/api/users/' + id + '?isHardDelete=' + isHardDelete, { method: 'DELETE' })
                .then(r => r.json())
                .then(data => {
                    if (data.code === 200) loadUsers();
                    else showDialog({ title: 'Error', message: data.message || 'Could not delete the user.', icon: 'warn' });
                })
                .catch(() => showDialog({ title: 'Error', message: 'Could not delete the user.', icon: 'warn' }));
        }
    });
}

// ===== Roles =====
function loadRoles() {
    return authFetch('/v1/api/role')
        .then(r => r.json())
        .then(data => { if (data.code === 200) { roles = data.data || []; renderRoles(); } })
        .catch(() => {});
}

function renderRoles() {
    document.getElementById('rolesTbody').innerHTML = roles.map(r => `<tr>
        <td class="cms-muted">${r.roleId}</td>
        <td>
            <span class="cms-badge ${r.roleName === 'ROLE_ADMIN' ? 'role-admin' : ''}">${esc(roleLabel(r.roleName))}</span>
            <span class="cms-muted" style="margin-left:8px; font-size:0.78rem;">${esc(r.roleName)}</span>
        </td>
        <td class="cms-col-actions"><button class="cms-row-btn" onclick="editRole(${r.roleId})">Rename</button></td>
    </tr>`).join('');
    document.getElementById('rolesEmpty').style.display = roles.length ? 'none' : 'block';
}

function openRoleModal(role) {
    editingRoleId = role ? role.roleId : null;
    document.getElementById('roleModalTitle').textContent = role ? 'Rename role' : 'Add role';
    document.getElementById('roleName').value = role ? role.roleName.replace(/^ROLE_/, '') : '';
    setError('roleModalError', '');
    roleModalInitial = val('roleName');
    updateRoleSaveState();
    document.getElementById('roleModal').classList.add('open');
}
function closeRoleModal() { document.getElementById('roleModal').classList.remove('open'); }
function editRole(id) { const r = roles.find(x => x.roleId === id); if (r) openRoleModal(r); }
// Renaming a role keeps Save disabled until the name actually differs.
function updateRoleSaveState() {
    const btn = document.getElementById('roleModalSave');
    btn.disabled = editingRoleId ? (val('roleName') === roleModalInitial) : false;
}

function saveRole() {
    const name = val('roleName');
    setError('roleModalError', '');

    const btn = document.getElementById('roleModalSave');
    btnLoading(btn, true);
    const body = JSON.stringify({ roleName: name });
    const req = editingRoleId
        ? authFetch('/v1/api/role/' + editingRoleId, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body })
        : authFetch('/v1/api/role', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body });

    req.then(r => r.json())
        .then(data => {
            if (data.code === 200 || data.code === 201) {
                closeRoleModal();
                loadRoles();
                // a renamed role may change how users display — refresh them too
                loadUsers();
            } else {
                setError('roleModalError', data.message || 'Could not save the role.');
            }
        })
        .catch(() => setError('roleModalError', 'Something went wrong. Please try again.'))
        .finally(() => btnLoading(btn, false, 'Save'));
}

// ===== Modal dismissal =====
// Only dismiss on a genuine backdrop click — one that both starts and ends on the
// overlay. Otherwise selecting text inside a field and releasing the mouse over the
// backdrop would close the modal unexpectedly.
['userModal', 'roleModal'].forEach(id => {
    const m = document.getElementById(id);
    let pressedOnOverlay = false;
    m.addEventListener('mousedown', e => { pressedOnOverlay = (e.target === m); });
    m.addEventListener('click', e => {
        if (e.target === m && pressedOnOverlay) m.classList.remove('open');
    });
});
// Keep each Save button in sync with whether the form has unsaved changes.
document.getElementById('userModal').addEventListener('input', updateUserSaveState);
document.getElementById('userModal').addEventListener('change', updateUserSaveState);
document.getElementById('roleModal').addEventListener('input', updateRoleSaveState);
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') { closeUserModal(); closeRoleModal(); closeDialog(); }
});

// ===== Boot =====
(function init() {
    initTheme();

    if (currentUser && currentUser.token && currentUser.role === 'ROLE_ADMIN') {
        showApp();
    } else {
        currentUser = null;
        localStorage.removeItem(CMS_SESSION_KEY);
        showLogin();
    }
})();
