package com.gdt.chess.api.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/games/{id}}.
 */
public record GameStateResponse(
        String gameId,
        String fen,
        String turn,
        String status,
        List<String> moves,
        int moveCount,
        String createdAt
) {}
