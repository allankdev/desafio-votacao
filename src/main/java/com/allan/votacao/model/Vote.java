package com.allan.votacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "votes",
        indexes = {
                @Index(name = "idx_votes_session", columnList = "session_id"),
                @Index(name = "idx_votes_session_cpf", columnList = "session_id,voter_cpf", unique = true)
        }
)
@Getter
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private VotingSession session;

    @Setter
    @Column(name = "voter_cpf", nullable = false, length = 11)
    private String voterCpf;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteOption choice;

    @Setter
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
