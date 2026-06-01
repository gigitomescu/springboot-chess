package com.gdt.chess.api.dto;

/**
 * Response body for {@code POST /api/games/{id}/moves}.
 */
public record MakeMoveResponse(
        String gameId,
        String move,
        String fen,
        String turn,
        String status,
        boolean gameOver
) {}
