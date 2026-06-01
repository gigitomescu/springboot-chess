import React, { useCallback, useEffect, useState } from 'react';
import styles from './ChessBoard.module.css';
import { Board } from '../types/chess.types';

interface ChessBoardProps {
  fen: string;
  disabled?: boolean;
  playerColor?: 'WHITE' | 'BLACK';
  /** UCI move to highlight (e.g. "e7e5") — used to show the last engine move */
  lastMove?: string | null;
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
const ChessBoard: React.FC<ChessBoardProps> = ({ fen, disabled = false, playerColor, lastMove, onMove }) => {
  const [board, setBoard]               = useState<Board>([]);
  const [selected, setSelected]         = useState<string | null>(null);

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

            return (
              <div
                key={sq}
                className={[
                  styles.square,
                  light ? styles.light : styles.dark,
                  sel              ? styles.selected      : '',
                  lastMoveHighlight ? styles.lastMove      : '',
                ].join(' ')}
                onClick={() => handleSquareClick(rankIdx, fileIdx)}
                data-square={sq}
              >
                {fileIdx === 0 && <span className={styles.rankLabel}>{rank}</span>}
                {rankIdx === 7 && <span className={styles.fileLabel}>{file}</span>}
                {piece && (
                  <span className={`${styles.piece} ${piece === piece.toUpperCase() ? styles.whitePiece : styles.blackPiece}`}>
                    {PIECE_SYMBOLS[piece]}
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
