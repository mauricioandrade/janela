package com.mauricio.janela.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTest {

    // RUN is comfortable from 10 to 22 °C feels-like.
    @ParameterizedTest
    @CsvSource({"9.9, COOL", "10, PLEASANT", "22, PLEASANT", "22.1, WARM", "26, WARM", "26.1, HOT", "31, HOT"})
    void judgesFeelsLikeAgainstTheActivityRange(double apparentTempC, Comfort comfort) {
        assertThat(Activity.RUN.comfortOf(apparentTempC)).isEqualTo(comfort);
    }
}
