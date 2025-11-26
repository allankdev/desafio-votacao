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

@Entity
@Table(
        name = "votes",
        indexes = {
                @Index(name = "idx_votes_session", columnList = "session_id"),
                @Index(name = "idx_votes_session_cpf", columnList = "session_id,voter_cpf", unique = true)
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private VotingSession session;

    @Column(name = "voter_cpf", nullable = false, length = 11)
    private String voterCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteOption choice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public VotingSession getSession() {
        return session;
    }

    public void setSession(VotingSession session) {
        this.session = session;
    }

    public String getVoterCpf() {
        return voterCpf;
    }

    public void setVoterCpf(String voterCpf) {
        this.voterCpf = voterCpf;
    }

    public VoteOption getChoice() {
        return choice;
    }

    public void setChoice(VoteOption choice) {
        this.choice = choice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
