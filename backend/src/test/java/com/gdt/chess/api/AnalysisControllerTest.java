package com.gdt.chess.api;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.model.MoveEvaluation;
import com.gdt.chess.analysis.service.AnalysisService;
import com.gdt.chess.api.controller.AnalysisController;
import com.gdt.chess.common.exception.EngineException;
import com.gdt.chess.common.exception.GlobalExceptionHandler;
import com.gdt.chess.common.mapper.GameMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link AnalysisController}.
 */
@WebMvcTest(AnalysisController.class)
@Import({GameMapper.class, GlobalExceptionHandler.class})
class AnalysisControllerTest {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Autowired MockMvc mockMvc;

    @MockBean AnalysisService analysisService;

    @Test
    @DisplayName("POST /api/analysis returns 200 with best move")
    void analyze_returns200() throws Exception {
        AnalysisResult result = AnalysisResult.builder()
                .fen(INITIAL_FEN)
                .bestMove("e2e4")
                .score(25)
                .isMate(false)
                .depth(12)
                .topMoves(List.of(
                        new MoveEvaluation("e2e4", 25, false, 0, List.of("e2e4", "e7e5"))
                ))
                .build();

        given(analysisService.analyze(anyString(), anyInt())).willReturn(result);

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fen\":\"" + INITIAL_FEN + "\",\"depth\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestMove").value("e2e4"))
                .andExpect(jsonPath("$.score").value(25))
                .andExpect(jsonPath("$.isMate").value(false))
                .andExpect(jsonPath("$.depth").value(12));
    }

    @Test
    @DisplayName("POST /api/analysis returns 503 when engine is unavailable")
    void analyze_engineUnavailable_returns503() throws Exception {
        given(analysisService.analyze(anyString(), anyInt()))
                .willThrow(new EngineException("Stockfish engine is not available"));

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fen\":\"" + INITIAL_FEN + "\",\"depth\":12}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("POST /api/analysis returns 400 when FEN is blank")
    void analyze_blankFen_returns400() throws Exception {
        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fen\":\"\",\"depth\":12}"))
                .andExpect(status().isBadRequest());
    }
}
