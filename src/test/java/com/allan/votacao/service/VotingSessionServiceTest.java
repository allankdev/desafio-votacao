package com.allan.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.dto.response.SessionResponse;
import com.allan.votacao.exception.SessionAlreadyOpenException;
import com.allan.votacao.model.SessionStatus;
import com.allan.votacao.model.VotingSession;
import java.time.Clock;
import java.time.LocalDateTime;
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

    @Autowired
    private Clock clock;

    @Test
    void shouldOpenSessionWithDefaultDuration() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Sessao", "Teste"));

        SessionResponse session = votingSessionService.openSession(agenda.id(), null);

        assertThat(session.status()).isEqualTo(SessionStatus.OPEN);
        assertThat(session.closesAt()).isEqualTo(session.openedAt().plusMinutes(1));
    }

    @Test
    void shouldNotAllowDoubleOpenSession() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Sessao 2", "Teste 2"));
        votingSessionService.openSession(agenda.id(), 2);

        assertThrows(SessionAlreadyOpenException.class,
                () -> votingSessionService.openSession(agenda.id(), 5));
    }

    @Test
    void shouldReportPendingWhenSessionIsMissing() {
        assertThat(votingSessionService.resolveStatus(null)).isEqualTo(SessionStatus.PENDING);
    }

    @Test
    void shouldReportClosedWhenSessionIsExpired() {
        VotingSession session = new VotingSession();
        session.open(LocalDateTime.now(clock).minusMinutes(5), 1);

        assertThat(votingSessionService.resolveStatus(session)).isEqualTo(SessionStatus.CLOSED);
    }

    @Test
    void shouldReportOpenWhileSessionStillActive() {
        VotingSession session = new VotingSession();
        session.open(LocalDateTime.now(clock), 5);

        assertThat(votingSessionService.resolveStatus(session)).isEqualTo(SessionStatus.OPEN);
    }
}
