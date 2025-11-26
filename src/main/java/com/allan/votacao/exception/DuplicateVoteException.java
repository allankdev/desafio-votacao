package com.allan.votacao.exception;

public class DuplicateVoteException extends RuntimeException {

    public DuplicateVoteException(String voterCpf) {
        super("O associado %s já votou nesta pauta".formatted(voterCpf));
    }
}
