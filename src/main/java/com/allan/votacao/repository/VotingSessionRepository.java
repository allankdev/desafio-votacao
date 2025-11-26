package com.allan.votacao.repository;

import com.allan.votacao.model.VotingSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {

    Optional<VotingSession> findByAgendaId(Long agendaId);

    List<VotingSession> findByAgendaIdIn(Iterable<Long> agendaIds);
}
