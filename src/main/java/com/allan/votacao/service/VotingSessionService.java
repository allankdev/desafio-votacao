package com.allan.votacao.service;

import com.allan.votacao.dto.response.SessionResponse;
import com.allan.votacao.exception.SessionAlreadyOpenException;
import com.allan.votacao.exception.SessionClosedException;
import com.allan.votacao.exception.SessionNotFoundException;
import com.allan.votacao.model.Agenda;
import com.allan.votacao.model.SessionStatus;
import com.allan.votacao.model.VotingSession;
import com.allan.votacao.repository.VotingSessionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingSessionService {

    private static final int DEFAULT_DURATION_MINUTES = 1;

    private final VotingSessionRepository votingSessionRepository;
    private final AgendaService agendaService;
    private final Clock clock;

    public VotingSessionService(VotingSessionRepository votingSessionRepository,
                                AgendaService agendaService,
                                Clock clock) {
        this.votingSessionRepository = votingSessionRepository;
        this.agendaService = agendaService;
        this.clock = clock;
    }

    @Transactional
    public SessionResponse openSession(Long agendaId, Integer durationInMinutes) {
        Agenda agenda = agendaService.requireById(agendaId);
        VotingSession session = votingSessionRepository.findByAgendaId(agendaId)
                .map(existing -> {
                    closeIfExpired(existing);
                    if (existing.getStatus() == SessionStatus.OPEN) {
                        throw new SessionAlreadyOpenException(agendaId);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    VotingSession created = new VotingSession();
                    created.setAgenda(agenda);
                    return created;
                });

        LocalDateTime openedAt = LocalDateTime.now(clock);
        int duration = (durationInMinutes == null || durationInMinutes <= 0)
                ? DEFAULT_DURATION_MINUTES
                : durationInMinutes;
        session.open(openedAt, duration);
        VotingSession saved = votingSessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional
    public VotingSession requireOpenSession(Long agendaId) {
        VotingSession session = votingSessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new SessionNotFoundException(agendaId));
        closeIfExpired(session);
        if (session.getStatus() != SessionStatus.OPEN) {
            throw new SessionClosedException(agendaId);
        }
        return session;
    }

    @Transactional(readOnly = true)
    public Optional<VotingSession> findByAgenda(Long agendaId) {
        return votingSessionRepository.findByAgendaId(agendaId);
    }

    @Transactional(readOnly = true)
    public SessionResponse describeSession(Long agendaId) {
        VotingSession session = votingSessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new SessionNotFoundException(agendaId));
        return toResponse(session);
    }

    public SessionStatus resolveStatus(VotingSession session) {
        if (session == null) {
            return SessionStatus.PENDING;
        }
        if (session.getStatus() == SessionStatus.CLOSED) {
            return SessionStatus.CLOSED;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return now.isAfter(session.getClosesAt()) ? SessionStatus.CLOSED : SessionStatus.OPEN;
    }

    private void closeIfExpired(VotingSession session) {
        if (session.getStatus() != SessionStatus.OPEN) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isAfter(session.getClosesAt())) {
            session.close(now);
            votingSessionRepository.save(session);
        }
    }

    private SessionResponse toResponse(VotingSession session) {
        return new SessionResponse(
                session.getId(),
                session.getAgenda().getId(),
                session.getOpenedAt(),
                session.getClosesAt(),
                session.getClosedAt(),
                session.getStatus());
    }
}
