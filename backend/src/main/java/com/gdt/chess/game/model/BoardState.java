package com.gdt.chess.game.model;

import java.util.Objects;

/**
 * Immutable value object representing the complete board state at a point in time.
 *
 * <p>The FEN (Forsyth–Edwards Notation) string is the single source of truth.
 * All derived properties (whose turn it is, move clocks, etc.) are parsed from
 * the FEN on demand.</p>
 */
public final class BoardState {

    /** Standard starting position FEN. */
    public static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final String fen;

    public BoardState(String fen) {
        Objects.requireNonNull(fen, "FEN cannot be null");
        this.fen = fen;
    }

    /** Returns a {@link BoardState} for the standard chess starting position. */
    public static BoardState initial() {
        return new BoardState(INITIAL_FEN);
    }

    /** The full FEN string for this board state. */
    public String getFen() { return fen; }

    /** Which player is to move next. */
    public PlayerColor getCurrentTurn() {
        String[] parts = fen.split(" ");
        return "w".equalsIgnoreCase(parts[1]) ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    /**
     * Number of half-moves since the last pawn advance or capture.
     * Used to detect the 50-move draw rule (value ≥ 100).
     */
    public int getHalfMoveClock() {
        return Integer.parseInt(fen.split(" ")[4]);
    }

    /** Full move number, incremented after Black's move. */
    public int getFullMoveNumber() {
        return Integer.parseInt(fen.split(" ")[5]);
    }

    @Override
    public String toString() { return fen; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardState bs)) return false;
        return fen.equals(bs.fen);
    }

    @Override
    public int hashCode() { return fen.hashCode(); }
}
