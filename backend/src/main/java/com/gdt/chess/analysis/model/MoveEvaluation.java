package com.gdt.chess.analysis.model;

import java.util.Collections;
import java.util.List;

/**
 * Evaluation of a single candidate move returned by the engine.
 *
 * <p>Each {@link MoveEvaluation} belongs to a multi-PV analysis result and
 * describes one line of play.</p>
 */
public final class MoveEvaluation {

    private final String move;
    private final int score;
    private final boolean isMate;
    private final int mateIn;
    private final List<String> line;

    public MoveEvaluation(String move, int score, boolean isMate, int mateIn, List<String> line) {
        this.move   = move;
        this.score  = score;
        this.isMate = isMate;
        this.mateIn = mateIn;
        this.line   = Collections.unmodifiableList(line);
    }

    /** Best move for this variation in UCI notation. */
    public String getMove()       { return move; }

    /** Centipawn score from the side-to-move perspective. */
    public int getScore()         { return score; }

    /** {@code true} when this variation leads to forced mate. */
    public boolean isMate()       { return isMate; }

    /** Moves until mate (positive = side to move wins, negative = side to move loses). */
    public int getMateIn()        { return mateIn; }

    /** Principal variation as a sequence of UCI moves. */
    public List<String> getLine() { return line; }
}
