package com.gdt.chess.game.model;

/**
 * All possible terminal and non-terminal states of a chess game.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVES,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_THREEFOLD_REPETITION,
    RESIGNED,
    TIMEOUT;

    /**
     * Returns {@code true} when the game has ended and no further moves are
     * possible.
     */
    public boolean isTerminal() {
        return this != IN_PROGRESS;
    }
}
