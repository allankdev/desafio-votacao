package com.allan.votacao.service;

import com.allan.votacao.dto.request.CreateAgendaRequest;
import com.allan.votacao.dto.response.AgendaResponse;
import com.allan.votacao.exception.AgendaNotFoundException;
import com.allan.votacao.model.Agenda;
import com.allan.votacao.repository.AgendaRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {

    private final AgendaRepository agendaRepository;
    private final Clock clock;

    public AgendaService(AgendaRepository agendaRepository, Clock clock) {
        this.agendaRepository = agendaRepository;
        this.clock = clock;
    }

    @Transactional
    public AgendaResponse create(CreateAgendaRequest request) {
        Agenda agenda = new Agenda();
        agenda.setTitle(request.title().trim());
        agenda.setDescription(request.description().trim());
        LocalDateTime now = LocalDateTime.now(clock);
        agenda.setCreatedAt(now);
        agenda.setUpdatedAt(now);
        return toResponse(agendaRepository.save(agenda));
    }

    @Transactional(readOnly = true)
    public Agenda requireById(Long id) {
        return agendaRepository.findById(id)
                .orElseThrow(() -> new AgendaNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public AgendaResponse findDetails(Long id) {
        return toResponse(requireById(id));
    }

    @Transactional(readOnly = true)
    public Page<AgendaResponse> list(Pageable pageable) {
        return agendaRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private AgendaResponse toResponse(Agenda agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getTitle(),
                agenda.getDescription(),
                agenda.getCreatedAt(),
                agenda.getUpdatedAt());
    }
}
