package com.gdt.chess.config;

import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.engine.StockfishEngine;
import com.gdt.chess.game.service.BoardService;
import com.gdt.chess.game.service.ChessLibBoardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level bean wiring.
 *
 * <p>Centralises interface-to-implementation bindings, keeping controllers and
 * services decoupled from concrete implementations (Dependency Inversion).</p>
 */
@Configuration
public class AppConfig {

    /**
     * Binds the {@link ChessEngine} interface to the {@link StockfishEngine}
     * implementation.  Swapping the engine only requires changing this binding.
     */
    @Bean
    public ChessEngine chessEngine(StockfishProperties properties) {
        return new StockfishEngine(properties);
    }

    /**
     * Binds the {@link BoardService} interface to the chesslib-backed
     * implementation.  Replacing the chess-rules library only requires a new
     * {@link BoardService} implementation here.
     */
    @Bean
    public BoardService boardService() {
        return new ChessLibBoardService();
    }
}
