package com.allan.votacao.exception;

public class SessionClosedException extends RuntimeException {

    public SessionClosedException(Long agendaId) {
        super("Sessão encerrada para a pauta %d".formatted(agendaId));
    }
}
