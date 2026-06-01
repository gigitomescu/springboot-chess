package com.gdt.chess.game.service;

import com.gdt.chess.api.dto.CreateGameRequest;
import com.gdt.chess.game.model.Game;

/**
 * Domain service for chess game lifecycle management.
 *
 * <p>Business logic lives here; controllers remain thin and delegate to this
 * interface.  Implementations must not expose internal domain objects through
 * the API layer directly.</p>
 */
public interface GameService {

    /**
     * Creates a new game.  Pass {@code null} or an empty {@link CreateGameRequest}
     * for a standard human-vs-human game.  Set {@code vsEngine=true} to play
     * against Stockfish at the configured skill level.
     *
     * @return the newly created {@link Game}
     */
    Game createGame(CreateGameRequest request);

    /**
     * Retrieves an existing game by its identifier.
     *
     * @param gameId unique game identifier
     * @return the {@link Game} with the given ID
     * @throws com.gdt.chess.common.exception.GameNotFoundException if not found
     */
    Game getGame(String gameId);

    /**
     * Validates and applies a move to the specified game.
     *
     * @param gameId  unique game identifier
     * @param uciMove move in UCI long-algebraic notation (e.g. {@code "e2e4"})
     * @return the updated {@link Game} after the move
     * @throws com.gdt.chess.common.exception.GameNotFoundException if the game does not exist
     * @throws com.gdt.chess.common.exception.InvalidMoveException  if the move is illegal
     */
    Game makeMove(String gameId, String uciMove);

    /**
     * Resigns the game on behalf of the active player.
     *
     * @param gameId unique game identifier
     * @return the updated {@link Game} with status {@code RESIGNED}
     */
    Game resign(String gameId);

    /**
     * Ends the game as a mutual draw agreement.
     *
     * @param gameId unique game identifier
     * @return the updated {@link Game} with status {@code DRAW_AGREEMENT}
     */
    Game offerDraw(String gameId);
}
