package com.gdt.chess.api.controller;

import com.gdt.chess.api.dto.CreateGameRequest;
import com.gdt.chess.api.dto.CreateGameResponse;
import com.gdt.chess.api.dto.GameStateResponse;
import com.gdt.chess.api.dto.MakeMoveRequest;
import com.gdt.chess.api.dto.MakeMoveResponse;
import com.gdt.chess.common.mapper.GameMapper;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for chess game lifecycle operations.
 *
 * <p>Thin by design: all business logic is delegated to {@link GameService}.
 * Only DTOs cross the API boundary; domain objects are never returned
 * directly.</p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/games} – create a new game</li>
 *   <li>{@code POST /api/games/{id}/moves} – submit a move</li>
 *   <li>{@code GET  /api/games/{id}} – retrieve game state</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameMapper  gameMapper;

    public GameController(GameService gameService, GameMapper gameMapper) {
        this.gameService = gameService;
        this.gameMapper  = gameMapper;
    }

    /**
     * Creates a new chess game.
     *
     * @return {@link CreateGameResponse} with the new game ID and initial FEN
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(@RequestBody(required = false) CreateGameRequest request) {
        Game game = gameService.createGame(request);
        return gameMapper.toCreateGameResponse(game);
    }

    /**
     * Submits a move for the specified game.
     *
     * @param gameId path variable identifying the game
     * @param request body containing the UCI move string
     * @return {@link MakeMoveResponse} with the updated FEN and game status
     */
    @PostMapping("/{gameId}/moves")
    public MakeMoveResponse makeMove(
            @PathVariable String gameId,
            @Valid @RequestBody MakeMoveRequest request) {
        Game game = gameService.makeMove(gameId, request.move());
        return gameMapper.toMakeMoveResponse(game, request.move());
    }

    /**
     * Returns the full state of a game including move history.
     *
     * @param gameId path variable identifying the game
     * @return {@link GameStateResponse}
     */
    @GetMapping("/{gameId}")
    public GameStateResponse getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return gameMapper.toGameStateResponse(game);
    }
}
