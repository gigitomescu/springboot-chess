package com.gdt.chess.game.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object representing a single chess move.
 *
 * <p>Moves are stored in UCI long algebraic notation
 * (e.g. {@code "e2e4"}, {@code "e7e8q"}).</p>
 */
public final class Move {

    private final String uci;
    private final String fenBefore;
    private final Instant playedAt;

    /**
     * @param uci       move in UCI notation (4 or 5 characters, lowercase)
     * @param fenBefore FEN of the position immediately before this move was played
     */
    public Move(String uci, String fenBefore) {
        Objects.requireNonNull(uci, "UCI move cannot be null");
        if (uci.length() < 4 || uci.length() > 5) {
            throw new IllegalArgumentException("Invalid UCI move length: " + uci);
        }
        this.uci       = uci.toLowerCase();
        this.fenBefore = fenBefore;
        this.playedAt  = Instant.now();
    }

    /** Full UCI notation, e.g. {@code "e2e4"} or {@code "e7e8q"}. */
    public String getUci() { return uci; }

    /** Source square in algebraic notation, e.g. {@code "e2"}. */
    public String getFrom() { return uci.substring(0, 2); }

    /** Destination square in algebraic notation, e.g. {@code "e4"}. */
    public String getTo() { return uci.substring(2, 4); }

    /**
     * Promotion piece character ({@code "q"}, {@code "r"}, {@code "b"},
     * {@code "n"}), or {@code null} if this is not a promotion move.
     */
    public String getPromotion() {
        return uci.length() == 5 ? uci.substring(4) : null;
    }

    /** FEN string of the board position before this move was executed. */
    public String getFenBefore() { return fenBefore; }

    /** Timestamp when the move was recorded. */
    public Instant getPlayedAt() { return playedAt; }

    @Override
    public String toString() { return uci; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move m)) return false;
        return uci.equals(m.uci) && Objects.equals(fenBefore, m.fenBefore);
    }

    @Override
    public int hashCode() { return Objects.hash(uci, fenBefore); }
}
