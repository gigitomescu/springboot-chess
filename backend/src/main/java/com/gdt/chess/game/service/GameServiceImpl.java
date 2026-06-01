package com.gdt.chess.game.service;

import com.gdt.chess.common.exception.GameNotFoundException;
import com.gdt.chess.common.exception.InvalidMoveException;
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

/**
 * In-memory implementation of {@link GameService}.
 *
 * <p>Games are stored in a {@link ConcurrentHashMap}.  This provides
 * thread-safe access without an external database (MVP scope).  Persistence
 * can be added by replacing this implementation with a JPA-backed one.</p>
 */
@Service
public class GameServiceImpl implements GameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final BoardService boardService;
    private final Map<String, Game> gameStore = new ConcurrentHashMap<>();

    public GameServiceImpl(BoardService boardService) {
        this.boardService = boardService;
    }

    // -------------------------------------------------------------------------
    // GameService
    // -------------------------------------------------------------------------

    @Override
    public Game createGame() {
        String id = UUID.randomUUID().toString();
        Game game = new Game(id);
        gameStore.put(id, game);
        log.info("Created game {}", id);
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

        String newFen       = boardService.applyMove(currentFen, normalised);
        GameStatus newStatus = boardService.determineStatus(newFen);

        Move move        = new Move(normalised, currentFen);
        BoardState state = new BoardState(newFen);

        game.applyMove(move, state, newStatus);
        log.info("Game {} – move {} applied, status: {}", gameId, normalised, newStatus);

        return game;
    }
}
