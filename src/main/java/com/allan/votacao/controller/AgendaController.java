package com.allan.votacao.controller;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.service.AgendaService;
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
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendaResponse create(@Valid @RequestBody CreateAgendaRequest request) {
        return agendaService.create(request);
    }

    @GetMapping
    public Page<AgendaResponse> list(Pageable pageable) {
        return agendaService.list(pageable);
    }

    @GetMapping("/{id}")
    public AgendaResponse findById(@PathVariable Long id) {
        return agendaService.findDetails(id);
    }
}
