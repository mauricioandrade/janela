package com.mauricio.janela.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API metadata for the generated OpenAPI document (/v3/api-docs) and Swagger UI (/swagger-ui.html).
 * Endpoint details live as annotations on the web layer, so the domain stays framework-free.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI janelaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Janela API")
                        .version("1.0")
                        .description("""
                                Finds the best time windows in the next days for an outdoor activity and explains \
                                them with a local Gemma model (template text when the model is offline). \
                                Times are local to the city; errors are RFC 9457 problem details.""")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
}
