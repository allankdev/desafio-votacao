package com.allan.votacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "voting_sessions")
@Getter
public class VotingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_id", nullable = false, unique = true)
    private Agenda agenda;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column(nullable = false)
    private LocalDateTime closesAt;

    @Column
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private SessionStatus status = SessionStatus.OPEN;

    public void open(LocalDateTime openedAt, long durationMinutes) {
        this.openedAt = openedAt;
        this.closesAt = openedAt.plusMinutes(durationMinutes);
        this.closedAt = null;
        this.status = SessionStatus.OPEN;
    }

    public void close(LocalDateTime moment) {
        this.closedAt = moment;
        this.status = SessionStatus.CLOSED;
    }

    public boolean isOpen(LocalDateTime moment) {
        if (status != SessionStatus.OPEN) {
            return false;
        }
        return !moment.isAfter(closesAt);
    }
}
