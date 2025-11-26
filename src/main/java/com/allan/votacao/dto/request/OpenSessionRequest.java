package com.allan.votacao.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OpenSessionRequest(
        @NotNull(message = "A duração deve ser informada em minutos")
        @Min(value = 1, message = "A duração mínima é de 1 minuto")
        @Max(value = 720, message = "A duração máxima de uma sessão é de 12 horas")
        Integer durationInMinutes) {
}
