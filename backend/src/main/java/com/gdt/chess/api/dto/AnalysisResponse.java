package com.gdt.chess.api.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/analysis} and the WebSocket
 * {@code /topic/analysis} subscription.
 */
public record AnalysisResponse(
        String fen,
        String bestMove,
        int score,
        boolean isMate,
        int mateIn,
        int depth,
        List<MoveEvaluationDto> topMoves
) {}
