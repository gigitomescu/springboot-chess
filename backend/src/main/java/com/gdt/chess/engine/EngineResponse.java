package com.gdt.chess.engine;

import java.util.Collections;
import java.util.List;

/**
 * Value object returned by a {@link ChessEngine} analysis call.
 *
 * <p>Encapsulates all information produced by one engine search: best move,
 * centipawn score (or mate distance), depth, and principal variation.</p>
 */
public final class EngineResponse {

    private final String bestMove;
    private final int score;
    private final boolean isMate;
    private final int mateIn;
    private final int depth;
    private final List<String> principalVariation;

    private EngineResponse(Builder builder) {
        this.bestMove           = builder.bestMove;
        this.score              = builder.score;
        this.isMate             = builder.isMate;
        this.mateIn             = builder.mateIn;
        this.depth              = builder.depth;
        this.principalVariation = Collections.unmodifiableList(builder.principalVariation);
    }

    /** Best move in UCI notation (e.g. {@code "e2e4"}), or {@code "(none)"} if no move exists. */
    public String getBestMove()  { return bestMove; }

    /** Centipawn score from the perspective of the side to move. */
    public int getScore()        { return score; }

    /** {@code true} when the score represents a forced mate distance. */
    public boolean isMate()      { return isMate; }

    /** Moves to mate (positive = current side mates, negative = opponent mates). */
    public int getMateIn()       { return mateIn; }

    /** Search depth actually reached. */
    public int getDepth()        { return depth; }

    /** Sequence of best moves (principal variation) in UCI notation. */
    public List<String> getPrincipalVariation() { return principalVariation; }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String bestMove = "(none)";
        private int score = 0;
        private boolean isMate = false;
        private int mateIn = 0;
        private int depth = 0;
        private List<String> principalVariation = Collections.emptyList();

        public Builder bestMove(String bestMove)                   { this.bestMove = bestMove; return this; }
        public Builder score(int score)                            { this.score = score; return this; }
        public Builder isMate(boolean isMate)                      { this.isMate = isMate; return this; }
        public Builder mateIn(int mateIn)                          { this.mateIn = mateIn; return this; }
        public Builder depth(int depth)                            { this.depth = depth; return this; }
        public Builder principalVariation(List<String> pv)        { this.principalVariation = pv; return this; }

        public EngineResponse build() { return new EngineResponse(this); }
    }
}
