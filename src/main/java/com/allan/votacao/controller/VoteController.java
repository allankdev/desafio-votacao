package com.allan.votacao.controller;

import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.dto.response.VoteResultResponse;
import com.allan.votacao.service.VoteService;
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
@RequestMapping("/api/v1/agendas/{agendaId}")
@Tag(name = "Votos e apuração", description = "Registro de votos únicos e consolidação do resultado")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registra voto",
            description = "Recebe voto 'SIM' ou 'NAO' validando CPF e unicidade por pauta.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Voto aceito",
                            content = @Content(schema = @Schema(implementation = VoteResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Pauta inexistente ou CPF não encontrado", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Associado já votou", content = @Content),
                    @ApiResponse(responseCode = "422", description = "CPF inválido para votar", content = @Content)
            }
    )
    public VoteResponse vote(
            @Parameter(description = "Identificador da pauta", required = true)
            @PathVariable Long agendaId,
                             @Valid @RequestBody VoteRequest request) {
        return voteService.registerVote(agendaId, request);
    }

    @GetMapping("/result")
    @Operation(
            summary = "Consulta o resultado",
            description = "Retorna totais de votos a favor/contra e o status da sessão.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado calculado",
                            content = @Content(schema = @Schema(implementation = VoteResultResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Pauta inexistente", content = @Content)
            }
    )
    public VoteResultResponse result(
            @Parameter(description = "Identificador da pauta", required = true)
            @PathVariable Long agendaId) {
        return voteService.summarizeAgenda(agendaId);
    }
}
