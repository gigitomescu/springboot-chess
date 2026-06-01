package com.gdt.chess.game.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a chess game.
 *
 * <p>Encapsulates all mutable state: current board position, move history, and
 * game status.  External code interacts through getters and the controlled
 * mutation method {@link #applyMove(Move, BoardState, GameStatus)}.</p>
 *
 * <p>Thread safety: {@code applyMove} is synchronised.  Reads are eventually
 * consistent; callers that need a consistent snapshot should synchronise
 * externally.</p>
 */
public final class Game {

    private final String id;
    private final Instant createdAt;
    private final List<Move> moveHistory;

    private volatile BoardState currentBoardState;
    private volatile GameStatus status;

    public Game(String id) {
        Objects.requireNonNull(id, "Game ID cannot be null");
        this.id                 = id;
        this.createdAt          = Instant.now();
        this.moveHistory        = new ArrayList<>();
        this.currentBoardState  = BoardState.initial();
        this.status             = GameStatus.IN_PROGRESS;
    }

    // -------------------------------------------------------------------------
    // Controlled mutation (called only by GameService after validation)
    // -------------------------------------------------------------------------

    /**
     * Applies a validated move to the game, advancing board state and status.
     *
     * <p>This method is the single write entry point, keeping mutation
     * centralised and auditable.</p>
     *
     * @param move          the validated move to record
     * @param newBoardState board state after the move
     * @param newStatus     game status after the move
     */
    public synchronized void applyMove(Move move, BoardState newBoardState, GameStatus newStatus) {
        moveHistory.add(move);
        currentBoardState = newBoardState;
        status            = newStatus;
    }

    // -------------------------------------------------------------------------
    // Read accessors
    // -------------------------------------------------------------------------

    public String getId()                   { return id; }
    public Instant getCreatedAt()           { return createdAt; }
    public BoardState getCurrentBoardState(){ return currentBoardState; }
    public GameStatus getStatus()           { return status; }
    public boolean isGameOver()             { return status.isTerminal(); }

    /** Returns an unmodifiable view of the move history. */
    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public int getMoveCount() { return moveHistory.size(); }

    @Override
    public String toString() {
        return "Game{id=" + id + ", moves=" + moveHistory.size() + ", status=" + status + "}";
    }
}
