package com.allan.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.dto.response.SessionResponse;
import com.allan.votacao.exception.SessionAlreadyOpenException;
import com.allan.votacao.model.SessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VotingSessionServiceTest {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private VotingSessionService votingSessionService;

    @Test
    void shouldOpenSessionWithDefaultDuration() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Sessão", "Teste"));

        SessionResponse session = votingSessionService.openSession(agenda.id(), null);

        assertThat(session.status()).isEqualTo(SessionStatus.OPEN);
        assertThat(session.closesAt()).isEqualTo(session.openedAt().plusMinutes(1));
    }

    @Test
    void shouldNotAllowDoubleOpenSession() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Sessão 2", "Teste 2"));
        votingSessionService.openSession(agenda.id(), 2);

        assertThrows(SessionAlreadyOpenException.class,
                () -> votingSessionService.openSession(agenda.id(), 5));
    }
}
