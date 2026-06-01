package com.gdt.chess.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for {@code POST /api/games/{id}/moves}.
 */
public record MakeMoveRequest(
        @NotBlank(message = "move must not be blank")
        @Pattern(regexp = "[a-hA-H][1-8][a-hA-H][1-8][qrbnQRBN]?",
                 message = "move must be in UCI notation, e.g. e2e4 or e7e8q")
        String move
) {}
