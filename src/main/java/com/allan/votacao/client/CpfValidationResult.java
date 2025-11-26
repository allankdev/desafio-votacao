package com.allan.votacao.client;

public record CpfValidationResult(String cpf, VotingEligibility status) {

    public boolean ableToVote() {
        return status == VotingEligibility.ABLE_TO_VOTE;
    }
}
