package com.allan.votacao.dto.response;

import com.allan.votacao.model.VoteOption;
import java.time.LocalDateTime;

public record VoteResponse(
        Long agendaId,
        Long sessionId,
        String voterCpf,
        VoteOption choice,
        LocalDateTime recordedAt) {
}
