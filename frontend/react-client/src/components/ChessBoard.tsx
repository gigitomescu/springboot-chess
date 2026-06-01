import React, { useCallback, useEffect, useState } from 'react';
import styles from './ChessBoard.module.css';
import { Board } from '../types/chess.types';

type MoveClassification = 'BRILLIANT' | 'EXCELLENT' | 'GOOD' | 'INACCURACY' | 'MISTAKE' | 'BLUNDER';

const CLASSIFICATION_LABEL: Record<MoveClassification, string> = {
  BRILLIANT:  '!!',
  EXCELLENT:  '✓',
  GOOD:       '!',
  INACCURACY: '?!',
  MISTAKE:    '?',
  BLUNDER:    '??',
};

interface ChessBoardProps {
  fen: string;
  disabled?: boolean;
  playerColor?: 'WHITE' | 'BLACK';
  /** UCI move to highlight (e.g. "e7e5") — used to show the last engine move */
  lastMove?: string | null;
  /** Quality badge to show on the player's last move destination square */
  moveClassification?: string | null;
  classificationSquare?: string | null;
  onMove: (uciMove: string) => void;
}

const PIECE_SYMBOLS: Record<string, string> = {
  K: '♔', Q: '♕', R: '♖', B: '♗', N: '♘', P: '♙',
  k: '♚', q: '♛', r: '♜', b: '♝', n: '♞', p: '♟',
};

const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
const RANKS = [8, 7, 6, 5, 4, 3, 2, 1];

function parseFen(fen: string): Board {
  const position = fen.split(' ')[0];
  return position.split('/').map(row => {
    const cells: (string | null)[] = [];
    for (const ch of row) {
      const n = parseInt(ch, 10);
      if (isNaN(n)) cells.push(ch);
      else for (let i = 0; i < n; i++) cells.push(null);
    }
    return cells;
  });
}

/**
 * Interactive chess board component.
 * Renders the position from a FEN string and emits UCI moves on user interaction.
 */
const ChessBoard: React.FC<ChessBoardProps> = ({
  fen, disabled = false, playerColor, lastMove,
  moveClassification, classificationSquare, onMove
}) => {
  const [board, setBoard]               = useState<Board>([]);
  const [selected, setSelected]         = useState<string | null>(null);
  const [dragFrom, setDragFrom]         = useState<string | null>(null);
  const [dragOver, setDragOver]         = useState<string | null>(null);

  // Flip ranks/files when playing as Black so player's pieces are at the bottom
  const flipped = playerColor === 'BLACK';
  const displayRanks = flipped ? [...RANKS].reverse() : RANKS;
  const displayFiles = flipped ? [...FILES].reverse() : FILES;

  useEffect(() => {
    setBoard(parseFen(fen));
    setSelected(null);
  }, [fen]);

  const squareName = (rankIdx: number, fileIdx: number): string =>
    `${displayFiles[fileIdx]}${displayRanks[rankIdx]}`;

  const isLight = (rankIdx: number, fileIdx: number): boolean => {
    const fileNum = FILES.indexOf(displayFiles[fileIdx]);
    const rankNum = RANKS.indexOf(displayRanks[rankIdx]);
    return (rankNum + fileNum) % 2 === 0;
  };

  const getOwnPiece = useCallback((rankIdx: number, fileIdx: number): string | null => {
    const piece = board[RANKS.indexOf(displayRanks[rankIdx])]?.[FILES.indexOf(displayFiles[fileIdx])];
    if (!piece) return null;
    if (playerColor) {
      const isWhitePiece = piece === piece.toUpperCase();
      if (playerColor === 'WHITE' && !isWhitePiece) return null;
      if (playerColor === 'BLACK' &&  isWhitePiece) return null;
    }
    return piece;
  }, [board, playerColor, displayRanks, displayFiles]);

  const handleSquareClick = useCallback((rankIdx: number, fileIdx: number) => {
    if (disabled) return;

    const square = squareName(rankIdx, fileIdx);
    const piece  = board[RANKS.indexOf(displayRanks[rankIdx])]?.[FILES.indexOf(displayFiles[fileIdx])];

    if (!selected) {
      if (piece) {
        // When playerColor is set, only allow selecting own pieces
        if (playerColor) {
          const isWhitePiece = piece === piece.toUpperCase();
          if (playerColor === 'WHITE' && !isWhitePiece) return;
          if (playerColor === 'BLACK' &&  isWhitePiece) return;
        }
        setSelected(square);
      }
    } else {
      if (selected === square) {
        setSelected(null);
      } else {
        onMove(selected + square);
        setSelected(null);
      }
    }
  }, [disabled, board, selected, onMove, playerColor, displayRanks, displayFiles]);

  const handleDragStart = useCallback((rankIdx: number, fileIdx: number, e: React.DragEvent) => {
    if (disabled || !getOwnPiece(rankIdx, fileIdx)) { e.preventDefault(); return; }
    const square = squareName(rankIdx, fileIdx);
    setDragFrom(square);
    setSelected(square);
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', square);
  }, [disabled, getOwnPiece, displayRanks, displayFiles]);

  const handleDragOver = useCallback((rankIdx: number, fileIdx: number, e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setDragOver(squareName(rankIdx, fileIdx));
  }, [displayRanks, displayFiles]);

  const handleDragLeave = useCallback(() => {
    setDragOver(null);
  }, []);

  const handleDrop = useCallback((rankIdx: number, fileIdx: number, e: React.DragEvent) => {
    e.preventDefault();
    const from = dragFrom;
    const to   = squareName(rankIdx, fileIdx);
    if (from && from !== to) onMove(from + to);
    setDragFrom(null);
    setDragOver(null);
    setSelected(null);
  }, [dragFrom, onMove, displayRanks, displayFiles]);

  const handleDragEnd = useCallback(() => {
    setDragFrom(null);
    setDragOver(null);
    setSelected(null);
  }, []);

  return (
    <div className={styles.boardContainer}>
      <div className={styles.board}>
        {displayRanks.map((rank, rankIdx) =>
          displayFiles.map((file, fileIdx) => {
            const sq    = squareName(rankIdx, fileIdx);
            const piece = board[RANKS.indexOf(rank)]?.[FILES.indexOf(file)];
            const light = isLight(rankIdx, fileIdx);
            const sel   = selected === sq;
            const lastMoveHighlight = lastMove
              ? sq === lastMove.slice(0, 2) || sq === lastMove.slice(2, 4)
              : false;
            const showBadge = classificationSquare === sq && moveClassification;
            const badgeClass = showBadge
              ? styles[`badge${moveClassification!.charAt(0) + moveClassification!.slice(1).toLowerCase()}`]
              : undefined;

            return (
              <div
                key={sq}
                className={[
                  styles.square,
                  light ? styles.light : styles.dark,
                  sel               ? styles.selected : '',
                  lastMoveHighlight ? styles.lastMove  : '',
                  dragOver === sq   ? styles.dragOver  : '',
                ].join(' ')}
                onClick={() => handleSquareClick(rankIdx, fileIdx)}
                onDragOver={e => handleDragOver(rankIdx, fileIdx, e)}
                onDragLeave={handleDragLeave}
                onDrop={e => handleDrop(rankIdx, fileIdx, e)}
                data-square={sq}
              >
                {fileIdx === 0 && <span className={styles.rankLabel}>{rank}</span>}
                {rankIdx === 7 && <span className={styles.fileLabel}>{file}</span>}
                {piece && (
                  <span
                    className={`${styles.piece} ${piece === piece.toUpperCase() ? styles.whitePiece : styles.blackPiece}`}
                    draggable={!disabled && !!getOwnPiece(rankIdx, fileIdx)}
                    onDragStart={e => handleDragStart(rankIdx, fileIdx, e)}
                    onDragEnd={handleDragEnd}
                  >
                    {PIECE_SYMBOLS[piece]}
                  </span>
                )}
                {showBadge && (
                  <span className={`${styles.moveBadge} ${badgeClass ?? ''}`}>
                    {CLASSIFICATION_LABEL[moveClassification as MoveClassification]}
                  </span>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default ChessBoard;
