package com.allan.votacao.support;

import com.allan.votacao.client.CpfValidationClient;
import com.allan.votacao.client.CpfValidationResult;
import com.allan.votacao.client.VotingEligibility;
import com.allan.votacao.exception.CpfValidationNotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Primary
public class StubCpfValidationClient implements CpfValidationClient {

    private VotingEligibility eligibility = VotingEligibility.ABLE_TO_VOTE;
    private boolean notFound;

    @Override
    public CpfValidationResult validate(String cpf) {
        if (notFound) {
            throw new CpfValidationNotFoundException(cpf);
        }
        return new CpfValidationResult(cpf, eligibility);
    }

    public void allowVoting() {
        this.eligibility = VotingEligibility.ABLE_TO_VOTE;
        this.notFound = false;
    }

    public void blockVoting() {
        this.eligibility = VotingEligibility.UNABLE_TO_VOTE;
        this.notFound = false;
    }

    public void markAsNotFound() {
        this.notFound = true;
    }
}
