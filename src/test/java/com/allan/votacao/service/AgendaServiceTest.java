package com.allan.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendaServiceTest {

    @Autowired
    private AgendaService agendaService;

    @Test
    void shouldCreateAgendaSuccessfully() {
        CreateAgendaRequest request = new CreateAgendaRequest(
                "Assembleia Geral",
                "Discussão da pauta anual");

        AgendaResponse response = agendaService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("Assembleia Geral");
        assertThat(response.createdAt()).isNotNull();
    }
}
