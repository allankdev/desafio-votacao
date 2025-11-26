package com.allan.votacao.dto.response;

import java.time.LocalDateTime;

public record AgendaResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
