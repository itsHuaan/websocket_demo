package com.example.websocket_demo.service.unclassified.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.MissingResourceException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageSource messageSource;

    public String getMessage(String key, String lang) {
        try {
            Locale locale = Locale.forLanguageTag(lang);
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            return "Invalid Message";
        } catch (MissingResourceException e) {
            return " Invalid Resource Bundle";
        } catch (IllegalArgumentException e) {
            return "Invalid Argument";
        } catch (NullPointerException e) {
            return "Message Source is null";
        }
    }
}
