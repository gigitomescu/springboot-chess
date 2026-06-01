package com.gdt.chess.engine;

import com.gdt.chess.common.exception.EngineException;
import com.gdt.chess.config.StockfishProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StockfishEngine}.
 *
 * <p>These tests do NOT start a real Stockfish process. They verify that:</p>
 * <ul>
 *   <li>The engine reports {@code available = false} when the executable is missing.</li>
 *   <li>{@link ChessEngine#analyze} throws {@link EngineException} when unavailable.</li>
 * </ul>
 */
class StockfishEngineTest {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private StockfishEngine engineWithPath(String path) {
        StockfishProperties props = new StockfishProperties();
        props.setPath(path);
        props.setDefaultDepth(5);
        props.setTimeoutSeconds(3);
        StockfishEngine engine = new StockfishEngine(props);
        engine.start(); // start() gracefully handles missing executable
        return engine;
    }

    @Test
    @DisplayName("isAvailable returns false when executable path is invalid")
    void isAvailable_invalidPath_returnsFalse() {
        StockfishEngine engine = engineWithPath("non-existent-stockfish.exe");
        assertThat(engine.isAvailable()).isFalse();
        engine.stop();
    }

    @Test
    @DisplayName("analyze throws EngineException when engine is unavailable")
    void analyze_unavailable_throwsEngineException() {
        StockfishEngine engine = engineWithPath("non-existent-stockfish.exe");

        assertThatThrownBy(() -> engine.analyze(INITIAL_FEN, 5))
                .isInstanceOf(EngineException.class)
                .hasMessageContaining("not available");

        engine.stop();
    }
}
