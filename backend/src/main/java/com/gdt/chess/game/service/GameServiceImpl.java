package com.gdt.chess.game.service;

import com.gdt.chess.api.dto.CreateGameRequest;
import com.gdt.chess.common.exception.GameNotFoundException;
import com.gdt.chess.common.exception.InvalidMoveException;
import com.gdt.chess.engine.ChessEngine;
import com.gdt.chess.engine.EngineResponse;
import com.gdt.chess.game.model.BoardState;
import com.gdt.chess.game.model.Game;
import com.gdt.chess.game.model.GameStatus;
import com.gdt.chess.game.model.Move;
import com.gdt.chess.game.model.MoveClassification;
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

        }

        int skillLevel = -1;
        if (vsEngine && request.engineElo() != null) {
            skillLevel = com.gdt.chess.config.StockfishProperties.eloToSkillLevel(request.engineElo());
            log.info("vs-engine game requested at ELO {} → Skill Level {}", request.engineElo(), skillLevel);
        }

        String id   = UUID.randomUUID().toString();
        Game   game = new Game(id, vsEngine, vsEngine ? playerColor : null, skillLevel);
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

        // ---- Pre-move analysis for move classification ----
        int    preMoveScore    = 0;
        String engineBestMove  = null;
        boolean doClassify     = game.isVsEngine() && engine.isAvailable();
        if (doClassify) {
            try {
                EngineResponse pre = engine.analyze(currentFen, 10);
                preMoveScore   = scoreOf(pre);
                engineBestMove = pre.getBestMove();
            } catch (Exception e) {
                log.debug("Pre-move analysis skipped: {}", e.getMessage());
                doClassify = false;
            }
        }

        String     newFen    = boardService.applyMove(currentFen, normalised);
        GameStatus newStatus = boardService.determineStatus(newFen);

        // ---- Post-move analysis for move classification ----
        MoveClassification classification = null;
        if (doClassify) {
            try {
                EngineResponse post    = engine.analyze(newFen, 10);
                int            postScore = scoreOf(post);
                // Both scores are from the side-to-move's perspective.
                // After the move the side to move changes, so postScore is the
                // opponent's score; centipawn loss for the mover = pre + post.
                int cpLoss = preMoveScore + postScore;
                classification = classify(normalised, engineBestMove, cpLoss);
            } catch (Exception e) {
                log.debug("Post-move analysis skipped: {}", e.getMessage());
            }
        }

        game.setLastEngineMove(null);
        game.setPlayerMoveFen(null);
        game.setPlayerMoveClassification(classification);
        game.applyMove(new Move(normalised, currentFen), new BoardState(newFen), newStatus);
        log.info("Game {} – move {} applied [{}], status: {}", gameId, normalised, classification, newStatus);

        if (game.isVsEngine() && !game.isGameOver() && engine.isAvailable()) {
            game.setPlayerMoveFen(newFen);
            applyEngineMove(game);
        }

        return game;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Returns the centipawn score clamped to ±30 000 for mate distances. */
    private static int scoreOf(EngineResponse r) {
        if (r.isMate()) return r.getMateIn() > 0 ? 30_000 : -30_000;
        return r.getScore();
    }

    /**
     * Classifies a move by centipawn loss relative to the engine's best choice.
     *
     * <p>Centipawn loss is {@code preMoveScore + postMoveScore} where both values
     * are from the side-to-move's perspective before and after the move
     * respectively.  A positive value means the position worsened for the mover.</p>
     */
    private static MoveClassification classify(String played, String best, int cpLoss) {
        // Brilliant: played the engine's top choice AND the position is at least
        // as good as before (no loss, meaning it was a strong/non-obvious best move).
        if (played.equals(best) && cpLoss <= 0)  return MoveClassification.BRILLIANT;
        if (cpLoss <= 30)                         return MoveClassification.EXCELLENT;
        if (cpLoss <= 100)                        return MoveClassification.GOOD;
        if (cpLoss <= 200)                        return MoveClassification.INACCURACY;
        if (cpLoss <= 400)                        return MoveClassification.MISTAKE;
        return MoveClassification.BLUNDER;
    }

    private void applyEngineMove(Game game) {
        String fen        = game.getCurrentBoardState().getFen();
        int    skill      = game.getEngineSkillLevel();
        String engineMove = (skill >= 0) ? engine.getBestMove(fen, skill) : engine.getBestMove(fen);
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
