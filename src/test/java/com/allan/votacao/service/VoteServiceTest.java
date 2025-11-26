package com.allan.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.exception.CpfUnableToVoteException;
import com.allan.votacao.exception.DuplicateVoteException;
import com.allan.votacao.model.VoteOption;
import com.allan.votacao.support.StubCpfValidationClient;
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

    private Long agendaId;

    @BeforeEach
    void setUp() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Votação", "Descrição"));
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
}
