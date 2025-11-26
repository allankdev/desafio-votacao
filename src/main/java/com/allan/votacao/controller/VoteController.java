package com.allan.votacao.controller;

import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.dto.response.VoteResultResponse;
import com.allan.votacao.service.VoteService;
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
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse vote(@PathVariable Long agendaId,
                             @Valid @RequestBody VoteRequest request) {
        return voteService.registerVote(agendaId, request);
    }

    @GetMapping("/result")
    public VoteResultResponse result(@PathVariable Long agendaId) {
        return voteService.summarizeAgenda(agendaId);
    }
}
