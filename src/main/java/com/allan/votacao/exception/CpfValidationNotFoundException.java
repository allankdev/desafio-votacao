package com.allan.votacao.exception;

public class CpfValidationNotFoundException extends RuntimeException {

    public CpfValidationNotFoundException(String cpf) {
        super("CPF %s não foi localizado na base externa".formatted(cpf));
    }
}
