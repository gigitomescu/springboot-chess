package com.gdt.chess.analysis.model;

import java.util.Collections;
import java.util.List;

/**
 * Domain object aggregating the engine's complete analysis of a position.
 *
 * <p>Never exposed directly through the API layer; the mapper converts this to
 * an {@link com.gdt.chess.api.dto.AnalysisResponse} DTO.</p>
 */
public final class AnalysisResult {

    private final String fen;
    private final String bestMove;
    private final int score;
    private final boolean isMate;
    private final int mateIn;
    private final int depth;
    private final List<MoveEvaluation> topMoves;

    private AnalysisResult(Builder builder) {
        this.fen      = builder.fen;
        this.bestMove = builder.bestMove;
        this.score    = builder.score;
        this.isMate   = builder.isMate;
        this.mateIn   = builder.mateIn;
        this.depth    = builder.depth;
        this.topMoves = Collections.unmodifiableList(builder.topMoves);
    }

    public String getFen()                      { return fen; }
    public String getBestMove()                 { return bestMove; }
    public int getScore()                       { return score; }
    public boolean isMate()                     { return isMate; }
    public int getMateIn()                      { return mateIn; }
    public int getDepth()                       { return depth; }
    public List<MoveEvaluation> getTopMoves()   { return topMoves; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String fen = "";
        private String bestMove = "(none)";
        private int score = 0;
        private boolean isMate = false;
        private int mateIn = 0;
        private int depth = 0;
        private List<MoveEvaluation> topMoves = Collections.emptyList();

        public Builder fen(String fen)                          { this.fen = fen; return this; }
        public Builder bestMove(String bestMove)                { this.bestMove = bestMove; return this; }
        public Builder score(int score)                         { this.score = score; return this; }
        public Builder isMate(boolean isMate)                   { this.isMate = isMate; return this; }
        public Builder mateIn(int mateIn)                       { this.mateIn = mateIn; return this; }
        public Builder depth(int depth)                         { this.depth = depth; return this; }
        public Builder topMoves(List<MoveEvaluation> topMoves) { this.topMoves = topMoves; return this; }

        public AnalysisResult build() { return new AnalysisResult(this); }
    }
}
