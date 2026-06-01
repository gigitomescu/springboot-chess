import React, { useCallback, useEffect, useState } from 'react';
import styles from './App.module.css';
import ChessBoard from './components/ChessBoard';
import AnalysisPanel from './components/AnalysisPanel';
import { createGame, makeMove } from './services/gameService';
import { GameStatus, MakeMoveResponse } from './types/chess.types';

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

/**
 * Root application component.
 * Manages game lifecycle and passes state down to child components.
 */
function App() {
  const [gameId, setGameId]           = useState<string | null>(null);
  const [fen, setFen]                 = useState(INITIAL_FEN);
  const [turn, setTurn]               = useState<'WHITE' | 'BLACK'>('WHITE');
  const [status, setStatus]           = useState<GameStatus>('IN_PROGRESS');
  const [moveHistory, setMoveHistory] = useState<string[]>([]);
  const [error, setError]             = useState<string | null>(null);
  const [loading, setLoading]         = useState(false);

  useEffect(() => { startNewGame(); }, []);

  const startNewGame = async () => {
    setLoading(true);
    setError(null);
    try {
      const game = await createGame();
      setGameId(game.gameId);
      setFen(game.fen);
      setTurn(game.turn);
      setStatus(game.status);
      setMoveHistory([]);
    } catch {
      setError('Failed to create game.');
    } finally {
      setLoading(false);
    }
  };

  const handleMove = useCallback(async (uciMove: string) => {
    if (!gameId || status !== 'IN_PROGRESS') return;
    setError(null);
    try {
      const res: MakeMoveResponse = await makeMove(gameId, uciMove);
      setFen(res.fen);
      setTurn(res.turn);
      setStatus(res.status);
      setMoveHistory(prev => [...prev, uciMove]);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail ?? 'Illegal move';
      setError(message);
    }
  }, [gameId, status]);

  const statusMessage = (): string => {
    switch (status) {
      case 'IN_PROGRESS': return `${turn === 'WHITE' ? 'White' : 'Black'} to move`;
      case 'CHECKMATE':   return 'Checkmate!';
      case 'STALEMATE':   return 'Stalemate – Draw';
      default:            return status.replace(/_/g, ' ');
    }
  };

  return (
    <div className={styles.appShell}>
      <header className={styles.header}>
        <h1>♟ Chess App</h1>
        <button className={styles.primary} onClick={startNewGame} disabled={loading}>
          {loading ? 'Loading…' : 'New Game'}
        </button>
      </header>

      <main className={styles.gameArea}>
        <section className={styles.boardSection}>
          <div className={`${styles.statusBar} ${status !== 'IN_PROGRESS' ? styles.gameOver : ''}`}>
            {statusMessage()}
          </div>

          <ChessBoard
            fen={fen}
            disabled={status !== 'IN_PROGRESS'}
            onMove={handleMove}
          />

          {error && <div className={styles.errorBanner}>{error}</div>}
        </section>

        <aside className={styles.sidePanel}>
          <div className={styles.moveHistory}>
            <h3>Move History</h3>
            <ol className={styles.moveList}>
              {moveHistory.map((move, i) => (
                <li key={i}>
                  {i % 2 === 0 && <span className={styles.moveNum}>{Math.floor(i / 2) + 1}.</span>}
                  {move}
                </li>
              ))}
            </ol>
          </div>

          <AnalysisPanel fen={fen} autoAnalyze={false} />
        </aside>
      </main>
    </div>
  );
}

export default App;
