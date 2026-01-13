package com.allan.votacao.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.model.Vote;
import com.allan.votacao.model.VoteOption;
import com.allan.votacao.model.VotingSession;
import com.allan.votacao.service.AgendaService;
import com.allan.votacao.repository.VotingSessionRepository;
import com.allan.votacao.service.VotingSessionService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VoteRepositoryIntegrationTest {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private VotingSessionService votingSessionService;

    @Autowired
    private VotingSessionRepository votingSessionRepository;

    @Autowired
    private Clock clock;

    @Test
    void shouldEnforceUniqueCpfPerSession() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Integração", "Duplicidade"));
        votingSessionService.openSession(agenda.id(), 5);
        VotingSession session = votingSessionRepository.findByAgendaId(agenda.id())
                .orElseThrow(() -> new IllegalStateException("session missing"));

        Vote first = new Vote();
        first.setSession(session);
        first.setVoterCpf("55555555555");
        first.setChoice(VoteOption.SIM);
        first.setCreatedAt(LocalDateTime.now(clock));
        voteRepository.saveAndFlush(first);

        Vote duplicate = new Vote();
        duplicate.setSession(session);
        duplicate.setVoterCpf("55555555555");
        duplicate.setChoice(VoteOption.NAO);
        duplicate.setCreatedAt(LocalDateTime.now(clock));

        assertThrows(DataIntegrityViolationException.class, () -> voteRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldCountVotesByChoiceInDbQuery() {
        AgendaResponse agenda = agendaService.create(new CreateAgendaRequest("Integração", "Contagem"));
        votingSessionService.openSession(agenda.id(), 5);
        VotingSession session = votingSessionRepository.findByAgendaId(agenda.id())
                .orElseThrow(() -> new IllegalStateException("session missing"));

        Vote favor = new Vote();
        favor.setSession(session);
        favor.setVoterCpf("11111111111");
        favor.setChoice(VoteOption.SIM);
        favor.setCreatedAt(LocalDateTime.now(clock));

        Vote contra = new Vote();
        contra.setSession(session);
        contra.setVoterCpf("22222222222");
        contra.setChoice(VoteOption.NAO);
        contra.setCreatedAt(LocalDateTime.now(clock));

        voteRepository.saveAll(List.of(favor, contra));
        voteRepository.flush();

        Map<VoteOption, Long> totals = voteRepository.countVotesByAgendaId(agenda.id())
                .stream()
                .collect(Collectors.toMap(VoteCountProjection::getChoice, VoteCountProjection::getTotal));

        assertThat(totals.get(VoteOption.SIM)).isEqualTo(1L);
        assertThat(totals.get(VoteOption.NAO)).isEqualTo(1L);
    }
}
