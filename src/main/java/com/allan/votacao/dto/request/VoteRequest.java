package com.allan.votacao.dto.request;

import com.allan.votacao.model.VoteOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VoteRequest(
        @NotBlank(message = "O CPF do associado é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
        String cpf,

        @NotNull(message = "O voto deve ser informado")
        VoteOption choice) {
}
