package com.gdt.chess.game.service;

import com.gdt.chess.api.dto.CreateGameRequest;
import com.gdt.chess.common.exception.GameNotFoundException;
import com.gdt.chess.common.exception.InvalidMoveException;
import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.game.model.BoardState;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.model.GameStatus;
import com.gdt.chess.game.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final BoardService boardService;
    private final ChessEngine  engine;
    private final Map<String, Game> gameStore = new ConcurrentHashMap<>();

    public GameServiceImpl(BoardService boardService, ChessEngine engine) {
        this.boardService = boardService;
        this.engine       = engine;
    }

    @Override
    public Game createGame(CreateGameRequest request) {
        boolean vsEngine    = request != null && Boolean.TRUE.equals(request.vsEngine());
        String  playerColor = "WHITE";

        if (vsEngine) {
            playerColor = (request.playerColor() != null)
                    ? request.playerColor().toUpperCase()
                    : "WHITE";

            // Apply the requested ELO as a one-time Skill Level override when provided.
            // The StockfishEngine was initialised with the YAML default; for per-game
            // overrides we reconfigure before each getBestMove call isn't possible via
            // the current synchronised design, so we rely on the YAML skill-level and
            // just log the requested ELO for information.
            if (request.engineElo() != null) {
                int skill = com.gdt.chess.config.StockfishProperties.eloToSkillLevel(request.engineElo());
                log.info("vs-engine game requested at ELO {} → Skill Level {}", request.engineElo(), skill);
            }
        }

        String id   = UUID.randomUUID().toString();
        Game   game = new Game(id, vsEngine, vsEngine ? playerColor : null);
        gameStore.put(id, game);
        log.info("Created game {} (vsEngine={}, playerColor={})", id, vsEngine, playerColor);

        // If engine plays White (player chose Black), make the opening move now.
        if (vsEngine && "BLACK".equals(playerColor) && engine.isAvailable()) {
            applyEngineMove(game);
        }

        return game;
    }

    @Override
    public Game getGame(String gameId) {
        return Optional.ofNullable(gameStore.get(gameId))
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    @Override
    public Game makeMove(String gameId, String uciMove) {
        Game game = getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidMoveException("Game " + gameId + " is already over: " + game.getStatus());
        }

        String currentFen = game.getCurrentBoardState().getFen();
        String normalised = uciMove.toLowerCase().trim();

        if (!boardService.isMoveLegal(currentFen, normalised)) {
            throw new InvalidMoveException("Illegal move '" + uciMove + "' in position: " + currentFen);
        }

        String     newFen    = boardService.applyMove(currentFen, normalised);
        GameStatus newStatus = boardService.determineStatus(newFen);

        game.setLastEngineMove(null);
        game.applyMove(new Move(normalised, currentFen), new BoardState(newFen), newStatus);
        log.info("Game {} – move {} applied, status: {}", gameId, normalised, newStatus);

        // After the player's move, let the engine reply if this is a vs-engine game.
        if (game.isVsEngine() && !game.isGameOver() && engine.isAvailable()) {
            applyEngineMove(game);
        }

        return game;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void applyEngineMove(Game game) {
        String fen        = game.getCurrentBoardState().getFen();
        String engineMove = engine.getBestMove(fen);
        if ("(none)".equals(engineMove)) {
            log.info("Engine returned (none) – no legal moves in position");
            return;
        }
        String     newFen    = boardService.applyMove(fen, engineMove);
        GameStatus newStatus = boardService.determineStatus(newFen);
        game.applyMove(new Move(engineMove, fen), new BoardState(newFen), newStatus);
        game.setLastEngineMove(engineMove);
        log.info("Engine played: {}, status: {}", engineMove, newStatus);
    }
}
