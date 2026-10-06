package com.mauricio.janela.domain.model;

import java.util.Arrays;

/**
 * Languages the narrative can be written in.
 */
public enum Language {

    PT("pt", "Brazilian Portuguese"),
    EN("en", "English");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public static Language fromCode(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
    }
}
