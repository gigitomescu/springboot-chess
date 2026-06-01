package com.gdt.chess.common.mapper;

import com.gdt.chess.analysis.model.AnalysisResult;
import com.gdt.chess.analysis.model.MoveEvaluation;
import com.gdt.chess.api.dto.AnalysisResponse;
import com.gdt.chess.api.dto.CreateGameResponse;
import com.gdt.chess.api.dto.GameStateResponse;
import com.gdt.chess.api.dto.MakeMoveResponse;
import com.gdt.chess.api.dto.MoveEvaluationDto;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.model.Move;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts domain objects to API DTOs.
 *
 * <p>Domain objects are never exposed directly through the API layer; this
 * mapper enforces that boundary (Anti-Corruption Layer).</p>
 */
@Component
public class GameMapper {

    public CreateGameResponse toCreateGameResponse(Game game) {
        return new CreateGameResponse(
                game.getId(),
                game.getCurrentBoardState().getFen(),
                game.getCurrentBoardState().getCurrentTurn().name(),
                game.getStatus().name(),
                game.getCreatedAt().toString()
        );
    }

    public GameStateResponse toGameStateResponse(Game game) {
        List<String> moves = game.getMoveHistory().stream()
                .map(Move::getUci)
                .toList();

        return new GameStateResponse(
                game.getId(),
                game.getCurrentBoardState().getFen(),
                game.getCurrentBoardState().getCurrentTurn().name(),
                game.getStatus().name(),
                moves,
                game.getMoveCount(),
                game.getCreatedAt().toString()
        );
    }

    public MakeMoveResponse toMakeMoveResponse(Game game, String uciMove) {
        return new MakeMoveResponse(
                game.getId(),
                uciMove,
                game.getCurrentBoardState().getFen(),
                game.getCurrentBoardState().getCurrentTurn().name(),
                game.getStatus().name(),
                game.isGameOver()
        );
    }

    public AnalysisResponse toAnalysisResponse(AnalysisResult result) {
        List<MoveEvaluationDto> topMoves = result.getTopMoves().stream()
                .map(this::toMoveEvaluationDto)
                .toList();

        return new AnalysisResponse(
                result.getFen(),
                result.getBestMove(),
                result.getScore(),
                result.isMate(),
                result.getMateIn(),
                result.getDepth(),
                topMoves
        );
    }

    private MoveEvaluationDto toMoveEvaluationDto(MoveEvaluation me) {
        return new MoveEvaluationDto(
                me.getMove(),
                me.getScore(),
                me.isMate(),
                me.getMateIn(),
                me.getLine()
        );
    }
}
