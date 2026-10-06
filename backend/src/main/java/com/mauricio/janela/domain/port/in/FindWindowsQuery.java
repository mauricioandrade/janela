package com.mauricio.janela.domain.port.in;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;

public record FindWindowsQuery(String city, Activity activity, int durationMinutes, int days, Language language) {
}
