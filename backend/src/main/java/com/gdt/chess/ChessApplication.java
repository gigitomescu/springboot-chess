package com.gdt.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.gdt.chess.config.StockfishProperties;

/**
 * Entry point for the Chess Application.
 *
 * <p>Provides a frontend-agnostic REST and WebSocket API for chess game
 * management and Stockfish-powered position analysis.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(StockfishProperties.class)
public class ChessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }
}
