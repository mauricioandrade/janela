package com.mauricio.janela.domain.model;

import java.util.Arrays;
import java.util.Locale;

/**
 * Languages the narrative can be written in.
 */
public enum Language {

    PT("pt", "Brazilian Portuguese", Locale.forLanguageTag("pt-BR")),
    EN("en", "English", Locale.ENGLISH);

    private final String code;
    private final String displayName;
    private final Locale locale;

    Language(String code, String displayName, Locale locale) {
        this.code = code;
        this.displayName = displayName;
        this.locale = locale;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public Locale locale() {
        return locale;
    }

    /** English readers get Fahrenheit, Portuguese readers Celsius; the forecast itself stays in Celsius. */
    public double temperatureFromCelsius(double celsius) {
        return this == EN ? celsius * 9 / 5 + 32 : celsius;
    }

    public String temperatureSymbol() {
        return this == EN ? "°F" : "°C";
    }

    public static Language fromCode(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
    }
}
