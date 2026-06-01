package com.gdt.chess.websocket;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.service.AnalysisService;
import com.gdt.chess.api.dto.AnalysisRequest;
import com.gdt.chess.api.dto.AnalysisResponse;
import com.gdt.chess.common.mapper.GameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket controller for real-time position analysis.
 *
 * <p>Clients connect to {@code /ws} (with SockJS fallback), subscribe to
 * {@code /topic/analysis}, and send analysis requests to
 * {@code /app/analyze}.</p>
 *
 * <pre>
 * Client → /app/analyze  (AnalysisRequest JSON)
 * Server → /topic/analysis (AnalysisResponse JSON)
 * </pre>
 */
@Controller
public class AnalysisWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisWebSocketController.class);

    private final AnalysisService analysisService;
    private final GameMapper      gameMapper;

    public AnalysisWebSocketController(AnalysisService analysisService, GameMapper gameMapper) {
        this.analysisService = analysisService;
        this.gameMapper      = gameMapper;
    }

    /**
     * Handles an analysis request from a WebSocket client.
     *
     * @param request contains FEN and depth
     * @return {@link AnalysisResponse} broadcast to all subscribers of
     *         {@code /topic/analysis}
     */
    @MessageMapping("/analyze")
    @SendTo("/topic/analysis")
    public AnalysisResponse analyze(AnalysisRequest request) {
        log.debug("WebSocket analysis request – fen={}, depth={}", request.fen(), request.depth());
        AnalysisResult result = analysisService.analyze(request.fen(), request.depth());
        return gameMapper.toAnalysisResponse(result);
    }
}
