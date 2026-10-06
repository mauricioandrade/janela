package com.mauricio.janela.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsBothEndpointsWithTheirParametersAndErrors() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Janela API"))
                .andExpect(jsonPath("$.paths['/api/windows'].get.parameters[*].name")
                        .value(hasItems("city", "cityId", "activity", "durationMinutes", "days", "lang")))
                .andExpect(jsonPath("$.paths['/api/windows'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/windows'].get.responses['503']").exists())
                .andExpect(jsonPath("$.paths['/api/cities'].get.parameters[*].name").value(hasItems("q", "lang")));
    }
}
