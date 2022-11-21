package com.example.screamlarkbot.lang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class Translator {

    private final ReloadableResourceBundleMessageSource messageSource;

    private Locale currentLocale = Locale.forLanguageTag("ru");

    public Locale getLocale() {
        return currentLocale;
    }

    public synchronized void setLocale(Locale locale) {
        this.currentLocale = locale;
        log.info("Current locale: {}", locale);
    }

    public synchronized String toLocale(String messageCode) {
        return messageSource.getMessage(messageCode, null, currentLocale);
    }
}
