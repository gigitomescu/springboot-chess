package com.gdt.chess.api.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/games/{id}/accuracy}.
 *
 * <p>Accuracy is expressed as a percentage (0–100) computed from the
 * win-rate loss per move, using the same logistic formula as Lichess:
 * <pre>
 *   winRate(cp)    = 100 / (1 + exp(-0.00368208 * cp))
 *   winRateLoss    = max(0, winRate(preCp) + winRate(postCp) - 100)
 *   moveAccuracy   = max(0, 103.1668 * exp(-0.04354 * winRateLoss) - 3.1669)
 *   playerAccuracy = average of all moveAccuracy values for that colour
 * </pre>
 * </p>
 *
 * @param gameId         the game that was reviewed
 * @param whiteAccuracy  overall accuracy for White (0–100, or -1 if unavailable)
 * @param blackAccuracy  overall accuracy for Black (0–100, or -1 if unavailable)
 * @param moves          per-move breakdown
 */
public record GameAccuracyResponse(
        String gameId,
        double whiteAccuracy,
        double blackAccuracy,
        List<MoveAccuracyDetail> moves
) {

    /**
     * Per-move accuracy detail.
     *
     * @param moveNumber  1-based full-move number
     * @param color       {@code "WHITE"} or {@code "BLACK"}
     * @param uci         move in UCI notation
     * @param preCp       centipawn evaluation before the move (mover's perspective)
     * @param postCp      centipawn evaluation after the move (opponent's perspective)
     * @param accuracy    accuracy for this individual move (0–100)
     */
    public record MoveAccuracyDetail(
            int moveNumber,
            String color,
            String uci,
            int preCp,
            int postCp,
            double accuracy
    ) {}
}
