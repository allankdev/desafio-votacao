package com.allan.votacao.controller;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agendas")
@Tag(name = "Agendas", description = "Cadastro e consulta de pautas")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria nova pauta",
            description = "Registra uma pauta com título e descrição para posterior abertura de sessão.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pauta criada",
                            content = @Content(schema = @Schema(implementation = AgendaResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos",
                            content = @Content)
            }
    )
    public AgendaResponse create(@Valid @RequestBody CreateAgendaRequest request) {
        return agendaService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "Lista pautas",
            description = "Retorna a listagem paginada de pautas cadastradas.",
            responses = @ApiResponse(responseCode = "200", description = "Lista retornada")
    )
    public Page<AgendaResponse> list(
            @Parameter(hidden = true) Pageable pageable) {
        return agendaService.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalha pauta por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pauta encontrada"),
                    @ApiResponse(responseCode = "404", description = "Pauta inexistente", content = @Content)
            }
    )
    public AgendaResponse findById(
            @Parameter(description = "Identificador da pauta", required = true)
            @PathVariable Long id) {
        return agendaService.findDetails(id);
    }
}
