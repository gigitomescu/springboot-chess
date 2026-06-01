package com.gdt.chess.game.model;

/**
 * Quality classification for a chess move, computed by comparing the
 * engine evaluation before and after the move (centipawn loss).
 *
 * <p>Displayed as an overlay badge on the destination square in the UI.</p>
 */
public enum MoveClassification {

    /** !! – the engine's top choice AND significantly improves the position. */
    BRILLIANT,

    /** ✓ – engine's top choice or within 30 cp of it. */
    EXCELLENT,

    /** Within 31–100 cp of the best move. */
    GOOD,

    /** ?! – 101–200 cp below the best move. */
    INACCURACY,

    /** ? – 201–400 cp below the best move. */
    MISTAKE,

    /** ?? – more than 400 cp below the best move. */
    BLUNDER
}
