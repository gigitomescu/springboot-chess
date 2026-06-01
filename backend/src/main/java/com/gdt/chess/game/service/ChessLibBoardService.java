package com.gdt.chess.game.service;

import com.gdt.chess.common.exception.InvalidMoveException;
import com.gdt.chess.game.model.GameStatus;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BoardService} backed by the <em>chesslib</em> library.
 *
 * <p>All chesslib-specific code is confined to this class.  Replacing the
 * chess-rule engine only requires a new {@link BoardService} implementation
 * registered in {@link com.gdt.chess.config.AppConfig}.</p>
 *
 * <p>A fresh {@link Board} instance is created for every operation so that
 * this service is stateless and thread-safe.</p>
 */
public class ChessLibBoardService implements BoardService {

    private static final Logger log = LoggerFactory.getLogger(ChessLibBoardService.class);

    @Override
    public boolean isMoveLegal(String fen, String uciMove) {
        Board board = loadBoard(fen);
        com.github.bhlangonijr.chesslib.move.Move move = parseMove(board, uciMove);
        try {
            MoveList legal = MoveGenerator.generateLegalMoves(board);
            return legal.contains(move);
        } catch (MoveGeneratorException e) {
            log.warn("MoveGenerator error for FEN '{}': {}", fen, e.getMessage());
            return false;
        }
    }

    @Override
    public String applyMove(String fen, String uciMove) {
        Board board = loadBoard(fen);
        com.github.bhlangonijr.chesslib.move.Move move = parseMove(board, uciMove);
        boolean applied = board.doMove(move);
        if (!applied) {
            throw new InvalidMoveException("Engine rejected move: " + uciMove);
        }
        return board.getFen();
    }

    @Override
    public GameStatus determineStatus(String fen) {
        Board board = loadBoard(fen);

        if (board.isMated())               return GameStatus.CHECKMATE;
        if (board.isStaleMate())           return GameStatus.STALEMATE;
        if (board.isInsufficientMaterial()) return GameStatus.DRAW_INSUFFICIENT_MATERIAL;
        if (board.isRepetition())          return GameStatus.DRAW_THREEFOLD_REPETITION;

        // 50-move rule: half-move clock ≥ 100
        try {
            int halfMove = Integer.parseInt(fen.split(" ")[4]);
            if (halfMove >= 100) return GameStatus.DRAW_FIFTY_MOVES;
        } catch (Exception ignored) { /* malformed FEN – ignore */ }

        return GameStatus.IN_PROGRESS;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Board loadBoard(String fen) {
        Board board = new Board();
        board.loadFromFen(fen);
        return board;
    }

    /**
     * Parses a UCI move string into a chesslib {@link com.github.bhlangonijr.chesslib.move.Move}.
     *
     * <p>Handles regular moves ({@code "e2e4"}) and promotion moves
     * ({@code "e7e8q"}).</p>
     */
    private com.github.bhlangonijr.chesslib.move.Move parseMove(Board board, String uci) {
        String lower = uci.toLowerCase();
        if (lower.length() < 4 || lower.length() > 5) {
            throw new InvalidMoveException("Invalid UCI move format: " + uci);
        }

        Square from = squareFrom(lower.substring(0, 2));
        Square to   = squareFrom(lower.substring(2, 4));

        if (lower.length() == 5) {
            Piece promotion = promotionPiece(lower.charAt(4), board.getSideToMove());
            return new com.github.bhlangonijr.chesslib.move.Move(from, to, promotion);
        }
        return new com.github.bhlangonijr.chesslib.move.Move(from, to, Piece.NONE);
    }

    private Square squareFrom(String sq) {
        try {
            return Square.valueOf(sq.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidMoveException("Invalid square: " + sq);
        }
    }

    private Piece promotionPiece(char c, Side side) {
        String prefix = side == Side.WHITE ? "WHITE_" : "BLACK_";
        String pieceName = switch (c) {
            case 'q' -> prefix + "QUEEN";
            case 'r' -> prefix + "ROOK";
            case 'b' -> prefix + "BISHOP";
            case 'n' -> prefix + "KNIGHT";
            default  -> throw new InvalidMoveException("Invalid promotion piece: " + c);
        };
        return Piece.valueOf(pieceName);
    }
}
