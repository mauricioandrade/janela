package com.mauricio.janela.domain.port.out;

import com.mauricio.janela.domain.model.Narrative;
import com.mauricio.janela.domain.model.NarrativeRequest;

/**
 * Turns pre-computed windows into text. Implementations must not choose or change windows.
 */
public interface NarrativeGenerator {

    Narrative generate(NarrativeRequest request);
}
