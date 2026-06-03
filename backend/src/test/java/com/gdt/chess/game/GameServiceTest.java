package com.gdt.chess.game;

import com.gdt.chess.common.exception.GameNotFoundException;
import com.gdt.chess.common.exception.InvalidMoveException;
import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.model.GameStatus;
import com.gdt.chess.config.StockfishProperties;
import com.gdt.chess.game.service.BoardService;
import com.gdt.chess.game.service.GameService;
import com.gdt.chess.game.service.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link GameServiceImpl}.
 *
 * <p>{@link BoardService} is mocked so that chess-rule logic is not exercised
 * here.</p>
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String AFTER_E2E4_FEN =
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";

    @Mock
    private BoardService boardService;

    @Mock
    private ChessEngine engine;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        StockfishProperties props = new StockfishProperties();
        props.setDefaultDepth(12);
        gameService = new GameServiceImpl(boardService, engine, props);
    }

    // -------------------------------------------------------------------------
    // createGame
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createGame returns a game with initial FEN and IN_PROGRESS status")
    void createGame_returnsInitialState() {
        Game game = gameService.createGame(null);

        assertThat(game).isNotNull();
        assertThat(game.getId()).isNotBlank();
        assertThat(game.getCurrentBoardState().getFen()).isEqualTo(INITIAL_FEN);
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getMoveCount()).isZero();
    }

    @Test
    @DisplayName("createGame generates unique IDs")
    void createGame_uniqueIds() {
        String id1 = gameService.createGame(null).getId();
        String id2 = gameService.createGame(null).getId();
        assertThat(id1).isNotEqualTo(id2);
    }

    // -------------------------------------------------------------------------
    // getGame
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getGame returns the game for a known ID")
    void getGame_returnsExistingGame() {
        Game created = gameService.createGame(null);
        Game retrieved = gameService.getGame(created.getId());
        assertThat(retrieved.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("getGame throws GameNotFoundException for unknown ID")
    void getGame_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> gameService.getGame("unknown-id"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining("unknown-id");
    }

    // -------------------------------------------------------------------------
    // makeMove
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("makeMove applies a legal move and updates board state")
    void makeMove_legalMove_updatesGame() {
        Game game = gameService.createGame(null);
        String gameId = game.getId();

        given(boardService.isMoveLegal(INITIAL_FEN, "e2e4")).willReturn(true);
        given(boardService.applyMove(INITIAL_FEN, "e2e4")).willReturn(AFTER_E2E4_FEN);
        given(boardService.determineStatus(AFTER_E2E4_FEN)).willReturn(GameStatus.IN_PROGRESS);

        Game updated = gameService.makeMove(gameId, "e2e4");

        assertThat(updated.getCurrentBoardState().getFen()).isEqualTo(AFTER_E2E4_FEN);
        assertThat(updated.getMoveCount()).isEqualTo(1);
        assertThat(updated.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("makeMove throws InvalidMoveException for illegal move")
    void makeMove_illegalMove_throwsException() {
        Game game = gameService.createGame(null);

        given(boardService.isMoveLegal(anyString(), eq("e2e5"))).willReturn(false);

        assertThatThrownBy(() -> gameService.makeMove(game.getId(), "e2e5"))
                .isInstanceOf(InvalidMoveException.class);
    }

    @Test
    @DisplayName("makeMove throws InvalidMoveException when game is already over")
    void makeMove_gameOver_throwsException() {
        Game game = gameService.createGame(null);
        String gameId = game.getId();

        // Simulate a completed game
        given(boardService.isMoveLegal(INITIAL_FEN, "e2e4")).willReturn(true);
        given(boardService.applyMove(INITIAL_FEN, "e2e4")).willReturn(AFTER_E2E4_FEN);
        given(boardService.determineStatus(AFTER_E2E4_FEN)).willReturn(GameStatus.CHECKMATE);

        gameService.makeMove(gameId, "e2e4"); // moves game to CHECKMATE

        assertThatThrownBy(() -> gameService.makeMove(gameId, "e7e5"))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already over");
    }

    @Test
    @DisplayName("makeMove detects CHECKMATE status")
    void makeMove_detectsCheckmate() {
        Game game = gameService.createGame(null);

        given(boardService.isMoveLegal(anyString(), anyString())).willReturn(true);
        given(boardService.applyMove(anyString(), anyString())).willReturn(AFTER_E2E4_FEN);
        given(boardService.determineStatus(AFTER_E2E4_FEN)).willReturn(GameStatus.CHECKMATE);

        Game updated = gameService.makeMove(game.getId(), "e2e4");

        assertThat(updated.getStatus()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(updated.isGameOver()).isTrue();
    }
}
