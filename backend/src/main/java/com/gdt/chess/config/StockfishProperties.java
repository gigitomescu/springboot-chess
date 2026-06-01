package com.gdt.chess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the Stockfish chess engine process.
 *
 * <p>Bound from the {@code stockfish.*} namespace in {@code application.yml}.</p>
 */
@ConfigurationProperties(prefix = "stockfish")
public class StockfishProperties {

    /** Absolute or relative path to the Stockfish executable. */
    private String path = "C:/stockfish/stockfish.exe";

    /** Default analysis depth when none is specified by the caller. */
    private int defaultDepth = 12;

    /** Maximum seconds to wait for a Stockfish response before timing out. */
    private int timeoutSeconds = 10;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public int getDefaultDepth() { return defaultDepth; }
    public void setDefaultDepth(int defaultDepth) { this.defaultDepth = defaultDepth; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
