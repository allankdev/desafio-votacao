package com.allan.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votingApi() {
        Info info = new Info()
                .title("Votação Cooperativa API")
                .description("""
                        API REST utilizada pelo aplicativo mobile para criação de pautas, abertura \
                        de sessões e registro/apuração de votos. Todos os contratos são versionados \
                        via path (/api/v1) e refletem os formatos JSON esperados pelo cliente.""")
                .version("v1")
                .contact(new Contact()
                        .name("Time Backend")
                        .email("backend@example.com"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente Local")));
    }
}
