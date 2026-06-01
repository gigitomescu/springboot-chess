package com.gdt.chess.common.exception;

/**
 * Thrown when a requested game cannot be found in the game store.
 */
public class GameNotFoundException extends RuntimeException {

    private final String gameId;

    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
        this.gameId = gameId;
    }

    public String getGameId() { return gameId; }
}
