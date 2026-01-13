package com.allan.votacao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.allan.votacao.dto.request.VoteRequest;
import com.allan.votacao.dto.response.VoteResponse;
import com.allan.votacao.exception.ApiExceptionHandler;
import com.allan.votacao.exception.CpfUnableToVoteException;
import com.allan.votacao.exception.DuplicateVoteException;
import com.allan.votacao.exception.SessionNotFoundException;
import com.allan.votacao.model.VoteOption;
import com.allan.votacao.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class VoteControllerTest {

    private final VoteService voteService = mock(VoteService.class);
    private final TestVoteController voteController = new TestVoteController(voteService);
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(voteController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldReturn201WhenVoteAccepted() throws Exception {
        VoteResponse response = new VoteResponse(1L, 2L, "12345678901", VoteOption.SIM, LocalDateTime.now());
        when(voteService.registerVote(any(), any())).thenReturn(response);

        VoteRequest request = new VoteRequest("12345678901", VoteOption.SIM);
        mockMvc.perform(post("/api/v1/agendas/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.choice").value("SIM"))
                .andExpect(jsonPath("$.agendaId").value(1));
    }

    @Test
    void shouldReturnConflictWhenDuplicateVote() throws Exception {
        when(voteService.registerVote(any(), any()))
                .thenThrow(new DuplicateVoteException("12345678901"));

        VoteRequest request = new VoteRequest("12345678901", VoteOption.SIM);
        mockMvc.perform(post("/api/v1/agendas/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnNotFoundWhenAgendaMissing() throws Exception {
        when(voteService.registerVote(any(), any()))
                .thenThrow(new SessionNotFoundException(1L));

        VoteRequest request = new VoteRequest("12345678901", VoteOption.SIM);
        mockMvc.perform(post("/api/v1/agendas/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn422WhenCpfUnable() throws Exception {
        when(voteService.registerVote(any(), any()))
                .thenThrow(new CpfUnableToVoteException("12345678901"));

        VoteRequest request = new VoteRequest("12345678901", VoteOption.SIM);
        mockMvc.perform(post("/api/v1/agendas/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void shouldReturn400WhenPayloadInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/agendas/1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static final class TestVoteController extends VoteController {

        TestVoteController(VoteService voteService) {
            super(voteService);
        }
    }
}
