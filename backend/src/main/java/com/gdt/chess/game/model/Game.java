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

    /** {@code true} when this game is played against the Stockfish engine. */
    private final boolean vsEngine;

    /** The human player's colour in a vs-engine game; {@code null} otherwise. */
    private final String playerColor;

    private volatile BoardState currentBoardState;
    private volatile GameStatus status;

    /** The last move made by the engine (set after each engine reply; {@code null} otherwise). */
    private volatile String lastEngineMove;

    /** FEN after the player's move, before the engine replied (null for human vs human). */
    private volatile String playerMoveFen;

    /** Quality classification of the player's last move; null when unavailable. */
    private volatile MoveClassification playerMoveClassification;

    /**
     * Stockfish Skill Level (0–20) for this game.
     * -1 means "use the engine's configured default from application.yml".
     */
    private final int engineSkillLevel;

    public Game(String id) {
        this(id, false, null, -1);
    }

    public Game(String id, boolean vsEngine, String playerColor) {
        this(id, vsEngine, playerColor, -1);
    }

    public Game(String id, boolean vsEngine, String playerColor, int engineSkillLevel) {
        Objects.requireNonNull(id, "Game ID cannot be null");
        this.id                 = id;
        this.createdAt          = Instant.now();
        this.moveHistory        = new ArrayList<>();
        this.currentBoardState  = BoardState.initial();
        this.status             = GameStatus.IN_PROGRESS;
        this.vsEngine           = vsEngine;
        this.playerColor        = playerColor;
        this.engineSkillLevel   = engineSkillLevel;
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

    /**
     * Forces the game into a terminal status (resign / draw agreement) without
     * adding a move to the history.
     */
    public synchronized void forceStatus(GameStatus newStatus) {
        this.status = newStatus;
    }

    // -------------------------------------------------------------------------
    // Read accessors
    // -------------------------------------------------------------------------

    public String getId()                   { return id; }
    public Instant getCreatedAt()           { return createdAt; }
    public BoardState getCurrentBoardState(){ return currentBoardState; }
    public GameStatus getStatus()           { return status; }
    public boolean isGameOver()             { return status.isTerminal(); }
    public boolean isVsEngine()             { return vsEngine; }
    public String getPlayerColor()          { return playerColor; }
    /** Stockfish Skill Level (0–20) for this game, or -1 to use the engine default. */
    public int getEngineSkillLevel()        { return engineSkillLevel; }
    public String getLastEngineMove()       { return lastEngineMove; }
    public void setLastEngineMove(String move) { this.lastEngineMove = move; }
    public String getPlayerMoveFen()        { return playerMoveFen; }
    public void setPlayerMoveFen(String fen){ this.playerMoveFen = fen; }
    public MoveClassification getPlayerMoveClassification()                           { return playerMoveClassification; }
    public void setPlayerMoveClassification(MoveClassification c)                     { this.playerMoveClassification = c; }

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
