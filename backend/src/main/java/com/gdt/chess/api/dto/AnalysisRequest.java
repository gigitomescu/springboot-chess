package com.gdt.chess.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/analysis}.
 */
public record AnalysisRequest(
        @NotBlank(message = "fen must not be blank")
        String fen,

        @Min(value = 1, message = "depth must be at least 1")
        @Max(value = 30, message = "depth must not exceed 30")
        int depth
) {}
