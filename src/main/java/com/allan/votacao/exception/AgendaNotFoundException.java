package com.allan.votacao.exception;

public class AgendaNotFoundException extends RuntimeException {

    public AgendaNotFoundException(Long id) {
        super("Pauta com id %d não encontrada".formatted(id));
    }
}
