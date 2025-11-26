package com.allan.votacao.dto.response;

import com.allan.votacao.model.SessionStatus;

public record VoteResultResponse(
        Long agendaId,
        long totalVotes,
        long votesInFavor,
        long votesAgainst,
        SessionStatus sessionStatus) {
}
