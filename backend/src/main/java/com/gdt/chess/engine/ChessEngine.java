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
     * Returns {@code true} when the underlying engine process is running and
     * ready to accept commands.
     */
    boolean isAvailable();
}
