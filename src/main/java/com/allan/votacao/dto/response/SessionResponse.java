package com.allan.votacao.dto.response;

import com.allan.votacao.model.SessionStatus;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        Long agendaId,
        LocalDateTime openedAt,
        LocalDateTime closesAt,
        LocalDateTime closedAt,
        SessionStatus status) {
}
