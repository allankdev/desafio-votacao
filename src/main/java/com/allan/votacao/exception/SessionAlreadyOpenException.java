package com.allan.votacao.exception;

public class SessionAlreadyOpenException extends RuntimeException {

    public SessionAlreadyOpenException(Long agendaId) {
        super("Já existe uma sessão de votação aberta para a pauta %d".formatted(agendaId));
    }
}
