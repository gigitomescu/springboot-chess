package com.gdt.chess.api.dto;

/**
 * Response body for {@code POST /api/games/{id}/moves}.
 *
 * @param engineMove the engine's reply in UCI notation, or {@code null} when
 *                   not playing against the engine or game ended on the
 *                   player's move
 */
public record MakeMoveResponse(
        String gameId,
        String move,
        String fen,
        String turn,
        String status,
        boolean gameOver,
        String engineMove
) {}
