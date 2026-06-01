package com.gdt.chess.analysis.service;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.model.MoveEvaluation;
import com.gdt.chess.config.StockfishProperties;
import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.engine.EngineResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default {@link AnalysisService} that delegates to the configured
 * {@link ChessEngine} and converts the response into an {@link AnalysisResult}.
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private final ChessEngine chessEngine;
    private final StockfishProperties properties;

    public AnalysisServiceImpl(ChessEngine chessEngine, StockfishProperties properties) {
        this.chessEngine = chessEngine;
        this.properties  = properties;
    }

    @Override
    public AnalysisResult analyze(String fen, int depth) {
        int searchDepth = depth > 0 ? depth : properties.getDefaultDepth();
        log.debug("Analysing position at depth {}: {}", searchDepth, fen);

        EngineResponse response = chessEngine.analyze(fen, searchDepth);

        MoveEvaluation topMove = new MoveEvaluation(
                response.getBestMove(),
                response.getScore(),
                response.isMate(),
                response.getMateIn(),
                response.getPrincipalVariation()
        );

        return AnalysisResult.builder()
                .fen(fen)
                .bestMove(response.getBestMove())
                .score(response.getScore())
                .isMate(response.isMate())
                .mateIn(response.getMateIn())
                .depth(response.getDepth())
                .topMoves(List.of(topMove))
                .build();
    }
}
