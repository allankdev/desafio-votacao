package com.allan.votacao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupLogger.class);

    @Value("${server.port:8080}")
    private int serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void logAccessInfo() {
        String baseUrl = "http://localhost:" + serverPort;
        LOGGER.info("API Votação disponível em {}", baseUrl);
        LOGGER.info("Swagger UI disponível em {}/swagger-ui.html", baseUrl);
        LOGGER.info("H2 console disponível em {}/h2-console", baseUrl);
    }
}
