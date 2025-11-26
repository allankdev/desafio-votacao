package com.allan.votacao.exception;

public class CpfUnableToVoteException extends RuntimeException {

    public CpfUnableToVoteException(String cpf) {
        super("O CPF %s não está apto para votar no momento".formatted(cpf));
    }
}
