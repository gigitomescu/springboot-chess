package com.gdt.chess.api.dto;

import java.util.List;

/**
 * DTO for a single candidate move inside an {@link AnalysisResponse}.
 */
public record MoveEvaluationDto(
        String move,
        int score,
        boolean isMate,
        int mateIn,
        List<String> line
) {}
