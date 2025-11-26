package com.allan.votacao.controller;

import com.allan.votacao.dto.request.OpenSessionRequest;
import com.allan.votacao.dto.response.SessionResponse;
import com.allan.votacao.service.VotingSessionService;
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
public class VotingSessionController {

    private final VotingSessionService votingSessionService;

    public VotingSessionController(VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse open(@PathVariable Long agendaId,
                                @Valid @RequestBody(required = false) OpenSessionRequest request) {
        Integer duration = request != null ? request.durationInMinutes() : null;
        return votingSessionService.openSession(agendaId, duration);
    }

    @GetMapping
    public SessionResponse details(@PathVariable Long agendaId) {
        return votingSessionService.describeSession(agendaId);
    }
}
