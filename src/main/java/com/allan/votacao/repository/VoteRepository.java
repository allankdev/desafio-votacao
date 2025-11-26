package com.allan.votacao.repository;

import com.allan.votacao.model.Vote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsBySessionIdAndVoterCpf(Long sessionId, String voterCpf);

    @Query("""
            select v.choice as choice, count(v) as total
            from Vote v
            where v.session.agenda.id = :agendaId
            group by v.choice
            """)
    List<VoteCountProjection> countVotesByAgendaId(@Param("agendaId") Long agendaId);
}
