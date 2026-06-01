package com.gdt.chess.engine;

/**
 * Abstraction for a chess analysis engine.
 *
 * <p>All engine-specific details (process management, protocol parsing) are
 * isolated behind this interface.  The rest of the application depends only on
 * this contract, not on Stockfish or any concrete engine.</p>
 *
 * <p>Implementations must be thread-safe.</p>
 */
public interface ChessEngine {

    /**
     * Analyses a position and returns the engine's evaluation.
     *
     * @param fen   FEN string of the position to analyse
     * @param depth search depth (plies)
     * @return {@link EngineResponse} containing the best move and evaluation
     * @throws com.gdt.chess.common.exception.EngineException if the engine
     *         is unavailable or returns an unexpected response
     */
    EngineResponse analyze(String fen, int depth);

    /**
     * Returns the best move for the given position at the configured Skill Level.
     * Uses a short fixed move-time (500 ms) for interactive play rather than
     * a fixed depth, so the engine feels responsive.
     *
     * @param fen FEN string of the position
     * @return UCI move string (e.g. {@code "e2e4"}) or {@code "(none)"} if
     *         the position has no legal moves
     * @throws com.gdt.chess.common.exception.EngineException if the engine is
     *         unavailable or returns an unexpected response
     */
    String getBestMove(String fen);

    /**
     * Returns the best move at an explicit Skill Level (0–20) rather than the
     * engine's configured default.  Implementations that do not support
     * per-call skill levels fall back to {@link #getBestMove(String)}.
     *
     * @param fen        FEN string of the position
     * @param skillLevel Stockfish Skill Level, 0 (≈1100 ELO) to 20 (full strength)
     */
    default String getBestMove(String fen, int skillLevel) {
        return getBestMove(fen);
    }

    /**
     * Returns {@code true} when the underlying engine process is running and
     * ready to accept commands.
     */
    boolean isAvailable();
}
