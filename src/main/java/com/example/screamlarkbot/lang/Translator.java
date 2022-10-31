package com.example.screamlarkbot.lang;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Translator {

    private final ReloadableResourceBundleMessageSource messageSource;

    private Locale currentLocale = Locale.forLanguageTag("ru");

    public synchronized void setLocale(Locale locale) {
        this.currentLocale = locale;
    }

    public synchronized String toLocale(String messageCode) {
        return messageSource.getMessage(messageCode, null, currentLocale);
    }
}
