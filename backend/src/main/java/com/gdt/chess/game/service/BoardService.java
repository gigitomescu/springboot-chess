package com.gdt.chess.game.service;

import com.gdt.chess.game.model.GameStatus;

/**
 * Abstraction for chess-rule operations: move legality checking, move
 * application, and game-status determination.
 *
 * <p>Isolating these concerns behind an interface means the chess-rule library
 * (chesslib) can be replaced without touching any other class.</p>
 */
public interface BoardService {

    /** Standard starting-position FEN. */
    String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /**
     * Returns {@code true} if {@code uciMove} is a legal move in the position
     * described by {@code fen}.
     *
     * @param fen     current position in FEN notation
     * @param uciMove move in UCI long-algebraic notation (e.g. {@code "e2e4"})
     */
    boolean isMoveLegal(String fen, String uciMove);

    /**
     * Applies {@code uciMove} to the position and returns the resulting FEN.
     *
     * <p>Callers must verify legality with {@link #isMoveLegal} before
     * calling this method.</p>
     *
     * @param fen     current position in FEN notation
     * @param uciMove legal move in UCI notation
     * @return FEN after the move
     */
    String applyMove(String fen, String uciMove);

    /**
     * Determines the {@link GameStatus} of the position after a move has been
     * applied.
     *
     * @param fen position to evaluate (FEN)
     * @return current game status
     */
    GameStatus determineStatus(String fen);
}
