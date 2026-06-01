package com.gdt.chess.api;

import com.gdt.chess.api.controller.GameController;
import com.gdt.chess.common.exception.GameNotFoundException;
import com.gdt.chess.common.exception.GlobalExceptionHandler;
import com.gdt.chess.common.exception.InvalidMoveException;
import com.gdt.chess.common.mapper.GameMapper;
import com.gdt.chess.game.model.BoardState;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.model.GameStatus;
import com.gdt.chess.game.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link GameController}.
 *
 * <p>Only the web layer is loaded; services are mocked.</p>
 */
@WebMvcTest(GameController.class)
@Import({GameMapper.class, GlobalExceptionHandler.class})
class GameControllerTest {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String AFTER_E2E4_FEN =
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean GameService gameService;

    // -------------------------------------------------------------------------
    // POST /api/games
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/games returns 201 with gameId and initial FEN")
    void createGame_returns201() throws Exception {
        Game game = new Game("test-id");
        given(gameService.createGame()).willReturn(game);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value("test-id"))
                .andExpect(jsonPath("$.fen").value(INITIAL_FEN))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // -------------------------------------------------------------------------
    // POST /api/games/{id}/moves
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/games/{id}/moves returns 200 with updated FEN")
    void makeMove_validMove_returns200() throws Exception {
        Game game = new Game("test-id");
        game.applyMove(
                new com.gdt.chess.game.model.Move("e2e4", INITIAL_FEN),
                new BoardState(AFTER_E2E4_FEN),
                GameStatus.IN_PROGRESS
        );
        given(gameService.makeMove(eq("test-id"), eq("e2e4"))).willReturn(game);

        mockMvc.perform(post("/api/games/test-id/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"move\":\"e2e4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("test-id"))
                .andExpect(jsonPath("$.fen").value(AFTER_E2E4_FEN))
                .andExpect(jsonPath("$.gameOver").value(false));
    }

    @Test
    @DisplayName("POST /api/games/{id}/moves returns 422 for invalid move")
    void makeMove_invalidMove_returns422() throws Exception {
        given(gameService.makeMove(eq("test-id"), any()))
                .willThrow(new InvalidMoveException("Illegal move: e2e5"));

        mockMvc.perform(post("/api/games/test-id/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"move\":\"e2e5\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/games/{id}/moves with bad UCI format returns 400")
    void makeMove_badFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/games/test-id/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"move\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/games/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/games/{id} returns 200 with game state")
    void getGame_returns200() throws Exception {
        Game game = new Game("test-id");
        given(gameService.getGame("test-id")).willReturn(game);

        mockMvc.perform(get("/api/games/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("test-id"))
                .andExpect(jsonPath("$.moveCount").value(0));
    }

    @Test
    @DisplayName("GET /api/games/{id} returns 404 for unknown game")
    void getGame_notFound_returns404() throws Exception {
        given(gameService.getGame("unknown")).willThrow(new GameNotFoundException("unknown"));

        mockMvc.perform(get("/api/games/unknown"))
                .andExpect(status().isNotFound());
    }
}
