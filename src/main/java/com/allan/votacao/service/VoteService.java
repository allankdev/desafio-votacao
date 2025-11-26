package com.allan.votacao.service;

import com.allan.votacao.client.CpfValidationClient;
import com.allan.votacao.client.CpfValidationResult;
import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.dto.response.VoteResultResponse;
import com.allan.votacao.exception.AgendaNotFoundException;
import com.allan.votacao.exception.CpfUnableToVoteException;
import com.allan.votacao.exception.DuplicateVoteException;
import com.allan.votacao.model.SessionStatus;
import com.allan.votacao.model.Vote;
import com.allan.votacao.model.VoteOption;
import com.allan.votacao.model.VotingSession;
import com.allan.votacao.repository.AgendaRepository;
import com.allan.votacao.repository.VoteCountProjection;
import com.allan.votacao.repository.VoteRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final AgendaRepository agendaRepository;
    private final VotingSessionService votingSessionService;
    private final CpfValidationClient cpfValidationClient;
    private final Clock clock;

    public VoteService(VoteRepository voteRepository,
                       AgendaRepository agendaRepository,
                       VotingSessionService votingSessionService,
                       CpfValidationClient cpfValidationClient,
                       Clock clock) {
        this.voteRepository = voteRepository;
        this.agendaRepository = agendaRepository;
        this.votingSessionService = votingSessionService;
        this.cpfValidationClient = cpfValidationClient;
        this.clock = clock;
    }

    @Transactional
    public VoteResponse registerVote(Long agendaId, VoteRequest request) {
        VotingSession session = votingSessionService.requireOpenSession(agendaId);
        String normalizedCpf = request.cpf().trim();

        if (voteRepository.existsBySessionIdAndVoterCpf(session.getId(), normalizedCpf)) {
            throw new DuplicateVoteException(normalizedCpf);
        }

        CpfValidationResult externalResult = cpfValidationClient.validate(normalizedCpf);
        if (!externalResult.ableToVote()) {
            throw new CpfUnableToVoteException(normalizedCpf);
        }

        Vote vote = new Vote();
        vote.setSession(session);
        vote.setVoterCpf(normalizedCpf);
        vote.setChoice(request.choice());
        vote.setCreatedAt(LocalDateTime.now(clock));

        Vote saved = voteRepository.save(vote);
        return new VoteResponse(
                session.getAgenda().getId(),
                session.getId(),
                saved.getVoterCpf(),
                saved.getChoice(),
                saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public VoteResultResponse summarizeAgenda(Long agendaId) {
        agendaRepository.findById(agendaId)
                .orElseThrow(() -> new AgendaNotFoundException(agendaId));

        VotingSession session = votingSessionService.findByAgenda(agendaId).orElse(null);
        SessionStatus status = votingSessionService.resolveStatus(session);

        List<VoteCountProjection> totals = voteRepository.countVotesByAgendaId(agendaId);
        Map<VoteOption, Long> counter = new EnumMap<>(VoteOption.class);
        totals.forEach(projection -> counter.put(
                projection.getChoice(),
                projection.getTotal()));

        long inFavor = counter.getOrDefault(VoteOption.SIM, 0L);
        long against = counter.getOrDefault(VoteOption.NAO, 0L);
        long total = inFavor + against;

        return new VoteResultResponse(agendaId, total, inFavor, against, status);
    }
}
