package com.allan.votacao.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(Long agendaId) {
        super("Nenhuma sessão foi registrada para a pauta %d".formatted(agendaId));
    }
}
