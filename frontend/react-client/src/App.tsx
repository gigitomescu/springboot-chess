import React, { useCallback, useEffect, useState } from 'react';
import styles from './App.module.css';
import ChessBoard from './components/ChessBoard';
import AnalysisPanel from './components/AnalysisPanel';
import EvalBar from './components/EvalBar';
import { createGame, makeMove } from './services/gameService';
import { analyzeRest } from './services/analysisService';
import { GameStatus, MakeMoveResponse } from './types/chess.types';

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
const ELO_OPTIONS = [300, 600, 800, 1000, 1200, 1500, 1800, 2000, 2500];

function App() {
  const [gameId, setGameId]           = useState<string | null>(null);
  const [fen, setFen]                 = useState(INITIAL_FEN);
  const [turn, setTurn]               = useState<'WHITE' | 'BLACK'>('WHITE');
  const [status, setStatus]           = useState<GameStatus>('IN_PROGRESS');
  const [moveHistory, setMoveHistory] = useState<string[]>([]);
  const [error, setError]             = useState<string | null>(null);
  const [loading, setLoading]         = useState(false);

  // vs-engine settings (chosen before game starts)
  const [vsEngine, setVsEngine]           = useState(false);
  const [playerColor, setPlayerColor]     = useState<'WHITE' | 'BLACK'>('WHITE');
  const [engineElo, setEngineElo]         = useState(800);
  const [engineThinking, setEngineThinking] = useState(false);
  const [isVsEngineGame, setIsVsEngineGame] = useState(false);
  const [myColor, setMyColor]             = useState<'WHITE' | 'BLACK'>('WHITE');

  // evaluation bar
  const [evalScore, setEvalScore]   = useState(0);
  const [evalIsMate, setEvalIsMate] = useState(false);
  const [evalMateIn, setEvalMateIn] = useState(0);

  useEffect(() => { startNewGame(); }, []);

  const refreshEval = (position: string) => {
    analyzeRest({ fen: position, depth: 14 })
      .then(r => { setEvalScore(r.score); setEvalIsMate(r.isMate); setEvalMateIn(r.mateIn); })
      .catch(() => { /* engine might not be running */ });
  };

  const startNewGame = async () => {
    setLoading(true);
    setError(null);
    setEvalScore(0); setEvalIsMate(false); setEvalMateIn(0);
    try {
      const game = await createGame(
        vsEngine ? { vsEngine: true, playerColor, engineElo } : {}
      );
      setGameId(game.gameId);
      setFen(game.fen);
      setTurn(game.turn);
      setStatus(game.status);
      setMoveHistory([]);
      setIsVsEngineGame(game.vsEngine);
      setMyColor(game.playerColor ?? 'WHITE');
      refreshEval(game.fen);
    } catch {
      setError('Failed to create game.');
    } finally {
      setLoading(false);
    }
  };

  const handleMove = useCallback(async (uciMove: string) => {
    if (!gameId || status !== 'IN_PROGRESS') return;
    if (isVsEngineGame && turn !== myColor) return; // not player's turn
    setError(null);
    setEngineThinking(isVsEngineGame);
    try {
      const res: MakeMoveResponse = await makeMove(gameId, uciMove);
      setFen(res.fen);
      setTurn(res.turn);
      setStatus(res.status);
      setMoveHistory(prev => {
        const next = [...prev, uciMove];
        if (res.engineMove) next.push(res.engineMove);
        return next;
      });
      refreshEval(res.fen);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail ?? 'Illegal move';
      setError(message);
    } finally {
      setEngineThinking(false);
    }
  }, [gameId, status, isVsEngineGame, turn, myColor]);

  const boardDisabled = status !== 'IN_PROGRESS' || engineThinking
    || (isVsEngineGame && turn !== myColor);

  const statusMessage = (): string => {
    if (engineThinking) return 'Engine is thinking…';
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

        <div className={styles.modeSelector}>
          <label className={styles.modeToggle}>
            <input
              type="checkbox"
              checked={vsEngine}
              onChange={e => setVsEngine(e.target.checked)}
            />
            vs Engine
          </label>

          {vsEngine && (
            <>
              <select
                className={styles.modeSelect}
                value={playerColor}
                onChange={e => setPlayerColor(e.target.value as 'WHITE' | 'BLACK')}
              >
                <option value="WHITE">Play White</option>
                <option value="BLACK">Play Black</option>
              </select>

              <select
                className={styles.modeSelect}
                value={engineElo}
                onChange={e => setEngineElo(Number(e.target.value))}
              >
                {ELO_OPTIONS.map(elo => (
                  <option key={elo} value={elo}>{elo} ELO</option>
                ))}
              </select>
            </>
          )}
        </div>

        <button className={styles.primary} onClick={startNewGame} disabled={loading}>
          {loading ? 'Loading…' : 'New Game'}
        </button>
      </header>

      <main className={styles.gameArea}>
        <section className={styles.boardSection}>
          <div className={`${styles.statusBar} ${status !== 'IN_PROGRESS' ? styles.gameOver : ''}`}>
            {statusMessage()}
          </div>

          <div className={styles.boardWithBar}>
            <EvalBar score={evalScore} isMate={evalIsMate} mateIn={evalMateIn} />
            <ChessBoard
              fen={fen}
              disabled={boardDisabled}
              onMove={handleMove}
            />
          </div>

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
