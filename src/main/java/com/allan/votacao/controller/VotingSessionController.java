package com.allan.votacao.controller;

import com.allan.votacao.dto.request.OpenSessionRequest;
import com.allan.votacao.dto.response.SessionResponse;
import com.allan.votacao.service.VotingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agendas/{agendaId}/sessions")
@Tag(name = "Sessões de votação", description = "Abertura e consulta de sessões ligadas às pautas")
public class VotingSessionController {

    private final VotingSessionService votingSessionService;

    public VotingSessionController(VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Abre sessão de votação",
            description = "Inicia uma sessão para uma pauta. Caso a sessão já exista e esteja aberta, retorna erro.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Sessão aberta",
                            content = @Content(schema = @Schema(implementation = SessionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Pauta inexistente", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Sessão já aberta", content = @Content)
            }
    )
    public SessionResponse open(
            @Parameter(description = "Identificador da pauta", required = true)
            @PathVariable Long agendaId,
                                @Valid @RequestBody(required = false) OpenSessionRequest request) {
        Integer duration = request != null ? request.durationInMinutes() : null;
        return votingSessionService.openSession(agendaId, duration);
    }

    @GetMapping
    @Operation(
            summary = "Consulta detalhes da sessão",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sessão encontrada"),
                    @ApiResponse(responseCode = "404", description = "Sessão inexistente", content = @Content)
            }
    )
    public SessionResponse details(
            @Parameter(description = "Identificador da pauta", required = true)
            @PathVariable Long agendaId) {
        return votingSessionService.describeSession(agendaId);
    }
}
