package com.gdt.chess.api.dto;

/**
 * Response body for {@code POST /api/games}.
 */
public record CreateGameResponse(
        String gameId,
        String fen,
        String turn,
        String status,
        String createdAt
) {}
