import React, { useCallback, useEffect, useState } from 'react';
import styles from './ChessBoard.module.css';
import { Board } from '../types/chess.types';

interface ChessBoardProps {
  fen: string;
  disabled?: boolean;
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
const ChessBoard: React.FC<ChessBoardProps> = ({ fen, disabled = false, onMove }) => {
  const [board, setBoard]               = useState<Board>([]);
  const [selected, setSelected]         = useState<string | null>(null);

  useEffect(() => {
    setBoard(parseFen(fen));
    setSelected(null);
  }, [fen]);

  const squareName = (rankIdx: number, fileIdx: number): string =>
    `${FILES[fileIdx]}${RANKS[rankIdx]}`;

  const isLight = (rankIdx: number, fileIdx: number): boolean =>
    (rankIdx + fileIdx) % 2 === 0;

  const handleSquareClick = useCallback((rankIdx: number, fileIdx: number) => {
    if (disabled) return;

    const square = squareName(rankIdx, fileIdx);
    const piece  = board[rankIdx]?.[fileIdx];

    if (!selected) {
      if (piece) setSelected(square);
    } else {
      if (selected === square) {
        setSelected(null);
      } else {
        onMove(selected + square);
        setSelected(null);
      }
    }
  }, [disabled, board, selected, onMove]);

  return (
    <div className={styles.boardContainer}>
      <div className={styles.board}>
        {RANKS.map((rank, rankIdx) =>
          FILES.map((file, fileIdx) => {
            const sq    = squareName(rankIdx, fileIdx);
            const piece = board[rankIdx]?.[fileIdx];
            const light = isLight(rankIdx, fileIdx);
            const sel   = selected === sq;

            return (
              <div
                key={sq}
                className={[
                  styles.square,
                  light ? styles.light : styles.dark,
                  sel   ? styles.selected : '',
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
