package com.allan.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.dto.response.VoteResultResponse;
import com.allan.votacao.exception.CpfUnableToVoteException;
import com.allan.votacao.exception.CpfValidationNotFoundException;
import com.allan.votacao.exception.DuplicateVoteException;
import com.allan.votacao.exception.SessionClosedException;
import com.allan.votacao.model.SessionStatus;
import com.allan.votacao.model.VoteOption;
import com.allan.votacao.model.VotingSession;
import com.allan.votacao.repository.VotingSessionRepository;
import com.allan.votacao.support.StubCpfValidationClient;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VoteServiceTest {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private VotingSessionService votingSessionService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private StubCpfValidationClient cpfValidationClient;

    @Autowired
    private VotingSessionRepository votingSessionRepository;

    @Autowired
    private Clock clock;

    private Long agendaId;

    @BeforeEach
    void setUp() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Votacao", "Descricao"));
        votingSessionService.openSession(agenda.id(), 5);
        this.agendaId = agenda.id();
        cpfValidationClient.allowVoting();
    }

    @Test
    void shouldRegisterVoteSuccessfully() {
        VoteResponse response = voteService.registerVote(agendaId, new VoteRequest("12345678901", VoteOption.SIM));

        assertThat(response.sessionId()).isNotNull();
        assertThat(response.choice()).isEqualTo(VoteOption.SIM);
    }

    @Test
    void shouldNotAllowDoubleVote() {
        voteService.registerVote(agendaId, new VoteRequest("22222222222", VoteOption.SIM));

        assertThrows(DuplicateVoteException.class,
                () -> voteService.registerVote(agendaId, new VoteRequest("22222222222", VoteOption.NAO)));
    }

    @Test
    void shouldPreventVoteWhenCpfIsNotAllowed() {
        cpfValidationClient.blockVoting();

        assertThrows(CpfUnableToVoteException.class,
                () -> voteService.registerVote(agendaId, new VoteRequest("99999999999", VoteOption.SIM)));
    }

    @Test
    void shouldSummarizeVotesAndKeepSessionOpen() {
        voteService.registerVote(agendaId, new VoteRequest("33333333333", VoteOption.SIM));
        voteService.registerVote(agendaId, new VoteRequest("44444444444", VoteOption.NAO));

        VoteResultResponse result = voteService.summarizeAgenda(agendaId);

        assertThat(result.totalVotes()).isEqualTo(2);
        assertThat(result.votesInFavor()).isEqualTo(1);
        assertThat(result.votesAgainst()).isEqualTo(1);
        assertThat(result.sessionStatus()).isEqualTo(SessionStatus.OPEN);
    }

    @Test
    void shouldReportPendingStatusWhenNoSessionOpened() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Nova pauta", "Sem sessao"));

        VoteResultResponse summary = voteService.summarizeAgenda(agenda.id());

        assertThat(summary.totalVotes()).isZero();
        assertThat(summary.sessionStatus()).isEqualTo(SessionStatus.PENDING);
    }

    @Test
    void shouldPropagateCpfValidationNotFound() {
        cpfValidationClient.markAsNotFound();

        assertThrows(CpfValidationNotFoundException.class,
                () -> voteService.registerVote(agendaId, new VoteRequest("11111111111", VoteOption.SIM)));
    }

    @Test
    void shouldRejectVoteWhenSessionClosed() {
        VotingSession session = votingSessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new IllegalStateException("session missing"));
        session.close(LocalDateTime.now(clock));
        votingSessionRepository.save(session);

        assertThrows(SessionClosedException.class,
                () -> voteService.registerVote(agendaId, new VoteRequest("55555555555", VoteOption.SIM)));
    }
}
