package com.allan.votacao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAgendaRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título deve conter no máximo 120 caracteres")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 4000, message = "A descrição deve conter no máximo 4000 caracteres")
        String description) {
}
