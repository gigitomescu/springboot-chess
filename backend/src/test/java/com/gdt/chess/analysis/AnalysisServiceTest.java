package com.gdt.chess.analysis;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.service.AnalysisService;
import com.gdt.chess.analysis.service.AnalysisServiceImpl;
import com.gdt.chess.config.StockfishProperties;
import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.engine.EngineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AnalysisServiceImpl}.
 *
 * <p>The {@link ChessEngine} is mocked so that no real Stockfish process is
 * started.</p>
 */
@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Mock
    private ChessEngine chessEngine;

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        StockfishProperties props = new StockfishProperties();
        props.setDefaultDepth(12);
        analysisService = new AnalysisServiceImpl(chessEngine, props);
    }

    @Test
    @DisplayName("analyze delegates to engine and maps response correctly")
    void analyze_delegatesToEngine_mapsResponse() {
        EngineResponse engineResponse = EngineResponse.builder()
                .bestMove("e2e4")
                .score(25)
                .isMate(false)
                .depth(12)
                .principalVariation(List.of("e2e4", "e7e5"))
                .build();

        given(chessEngine.analyze(INITIAL_FEN, 12)).willReturn(engineResponse);

        AnalysisResult result = analysisService.analyze(INITIAL_FEN, 12);

        assertThat(result.getFen()).isEqualTo(INITIAL_FEN);
        assertThat(result.getBestMove()).isEqualTo("e2e4");
        assertThat(result.getScore()).isEqualTo(25);
        assertThat(result.isMate()).isFalse();
        assertThat(result.getDepth()).isEqualTo(12);
        assertThat(result.getTopMoves()).hasSize(1);
        assertThat(result.getTopMoves().get(0).getLine()).containsExactly("e2e4", "e7e5");
    }

    @Test
    @DisplayName("analyze uses default depth when 0 is provided")
    void analyze_zeroDepth_usesDefault() {
        EngineResponse engineResponse = EngineResponse.builder()
                .bestMove("e2e4").depth(12).build();

        given(chessEngine.analyze(INITIAL_FEN, 12)).willReturn(engineResponse);

        analysisService.analyze(INITIAL_FEN, 0);

        verify(chessEngine).analyze(INITIAL_FEN, 12);
    }

    @Test
    @DisplayName("analyze maps mate score correctly")
    void analyze_mateScore_mapsMateFields() {
        EngineResponse engineResponse = EngineResponse.builder()
                .bestMove("f7f8q")
                .isMate(true)
                .mateIn(2)
                .score(30000)
                .depth(10)
                .build();

        given(chessEngine.analyze(INITIAL_FEN, 10)).willReturn(engineResponse);

        AnalysisResult result = analysisService.analyze(INITIAL_FEN, 10);

        assertThat(result.isMate()).isTrue();
        assertThat(result.getMateIn()).isEqualTo(2);
    }
}
