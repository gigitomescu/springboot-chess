package com.gdt.chess.game.model;

/**
 * Represents which side of the board a player controls.
 */
public enum PlayerColor {
    WHITE,
    BLACK;

    /** Returns the opposing colour. */
    public PlayerColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /** Returns the FEN character used for this colour ({@code 'w'} or {@code 'b'}). */
    public char fenChar() {
        return this == WHITE ? 'w' : 'b';
    }
}
