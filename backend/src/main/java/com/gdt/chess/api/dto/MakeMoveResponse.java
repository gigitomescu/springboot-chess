package com.gdt.chess.api.dto;

import com.gdt.chess.game.model.MoveClassification;

/**
 * Response body for {@code POST /api/games/{id}/moves}.
 *
 * @param engineMove the engine's reply in UCI notation, or {@code null} when
 *                   not playing against the engine or game ended on the
 *                   player's move
 * @param moveClassification quality of the player's move (BRILLIANT → BLUNDER),
 *                            or {@code null} when the engine is unavailable
 */
public record MakeMoveResponse(
        String gameId,
        String move,
        String fen,
        String turn,
        String status,
        boolean gameOver,
        String engineMove,
        /** FEN after the player's move but before the engine replied; null in human vs human. */
        String playerMoveFen,
        MoveClassification moveClassification
) {}
