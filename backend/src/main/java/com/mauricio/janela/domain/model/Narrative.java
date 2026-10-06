package com.mauricio.janela.domain.model;

/**
 * Human-readable explanation of the windows. {@code model} names the LLM that wrote it, or is null
 * when the text came from the offline template.
 */
public record Narrative(String text, boolean aiGenerated, String model) {

    public static Narrative fromModel(String text, String model) {
        return new Narrative(text, true, model);
    }

    public static Narrative fromTemplate(String text) {
        return new Narrative(text, false, null);
    }
}
