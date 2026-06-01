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

    /**
     * Stockfish Skill Level (0–20). 0 = weakest (≈1100 ELO, blunder-prone),
     * 20 = full engine strength. Use {@link #eloToSkillLevel(int)} to convert
     * an approximate ELO target to this value.
     */
    private int skillLevel = 0;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public int getDefaultDepth() { return defaultDepth; }
    public void setDefaultDepth(int defaultDepth) { this.defaultDepth = defaultDepth; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public int getSkillLevel() { return skillLevel; }
    public void setSkillLevel(int skillLevel) { this.skillLevel = Math.max(0, Math.min(20, skillLevel)); }

    /** Maps a rough target ELO to a Stockfish Skill Level (0–20). ≤800 → 0, ≥3200 → 20. */
    public static int eloToSkillLevel(int elo) {
        if (elo <= 800)  return 0;
        if (elo >= 3200) return 20;
        return (int) Math.round((elo - 800.0) / 120.0);
    }
}
