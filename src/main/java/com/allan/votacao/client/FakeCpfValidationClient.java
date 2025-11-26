package com.allan.votacao.client;

import com.allan.votacao.exception.CpfValidationNotFoundException;
import java.util.Objects;
import java.util.Random;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class FakeCpfValidationClient implements CpfValidationClient {

    private final Random random;

    public FakeCpfValidationClient(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public CpfValidationResult validate(String cpf) {
        Objects.requireNonNull(cpf, "cpf");
        boolean cpfExists = random.nextBoolean();
        if (!cpfExists) {
            throw new CpfValidationNotFoundException(cpf);
        }
        VotingEligibility eligibility = random.nextBoolean()
                ? VotingEligibility.ABLE_TO_VOTE
                : VotingEligibility.UNABLE_TO_VOTE;
        return new CpfValidationResult(cpf, eligibility);
    }
}
