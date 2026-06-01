package com.gdt.chess.analysis.service;

import com.gdt.chess.analysis.model.AnalysisResult;

/**
 * Domain service for chess position analysis.
 *
 * <p>Coordinates between the {@link com.gdt.chess.engine.ChessEngine} and
 * the API layer, converting raw engine output into structured domain objects.</p>
 */
public interface AnalysisService {

    /**
     * Analyses the given position to the requested depth.
     *
     * @param fen   position to analyse in FEN notation
     * @param depth search depth (number of plies); use {@code 0} for the
     *              configured default
     * @return structured {@link AnalysisResult}
     * @throws com.gdt.chess.common.exception.EngineException if the engine
     *         is unavailable or fails
     */
    AnalysisResult analyze(String fen, int depth);
}
