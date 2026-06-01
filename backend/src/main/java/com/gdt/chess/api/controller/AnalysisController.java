package com.gdt.chess.api.controller;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.service.AnalysisService;
import com.gdt.chess.api.dto.AnalysisRequest;
import com.gdt.chess.api.dto.AnalysisResponse;
import com.gdt.chess.common.mapper.GameMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for synchronous position analysis.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/analysis} – analyse a FEN position</li>
 * </ul>
 *
 * <p>For streaming analysis updates, connect via WebSocket at {@code /ws} and
 * send to {@code /app/analyze}; responses are broadcast to
 * {@code /topic/analysis}.</p>
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final GameMapper      gameMapper;

    public AnalysisController(AnalysisService analysisService, GameMapper gameMapper) {
        this.analysisService = analysisService;
        this.gameMapper      = gameMapper;
    }

    /**
     * Analyses the given position to the requested depth.
     *
     * @param request contains FEN string and desired depth
     * @return {@link AnalysisResponse} with best move and evaluation
     */
    @PostMapping
    public AnalysisResponse analyze(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResult result = analysisService.analyze(request.fen(), request.depth());
        return gameMapper.toAnalysisResponse(result);
    }
}
