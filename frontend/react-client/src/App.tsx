import React, { useCallback, useEffect, useRef, useState } from 'react';
import styles from './App.module.css';
import ChessBoard from './components/ChessBoard';
import AnalysisPanel from './components/AnalysisPanel';
import EvalBar from './components/EvalBar';
import GameClock from './components/GameClock';
import { createGame, makeMove, resign, offerDraw } from './services/gameService';
import { analyzeRest } from './services/analysisService';
import { GameStatus, MakeMoveResponse } from './types/chess.types';
import { useChessSound, wasCapture } from './hooks/useChessSound';

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
const ELO_OPTIONS = [300, 600, 800, 1000, 1200, 1500, 1800, 2000, 2500];

const TIME_CONTROLS = [
  { label: 'Unlimited', seconds: 0 },
  { label: '1 min',     seconds: 60 },
  { label: '3 min',     seconds: 180 },
  { label: '5 min',     seconds: 300 },
  { label: '10 min',    seconds: 600 },
  { label: '15 min',    seconds: 900 },
];

function App() {
  const [gameId, setGameId]           = useState<string | null>(null);
  const [fen, setFen]                 = useState(INITIAL_FEN);
  const [turn, setTurn]               = useState<'WHITE' | 'BLACK'>('WHITE');
  const [status, setStatus]           = useState<GameStatus>('IN_PROGRESS');
  const [moveHistory, setMoveHistory] = useState<string[]>([]);
  const [error, setError]             = useState<string | null>(null);
  const [loading, setLoading]         = useState(false);

  // vs-engine settings
  const [vsEngine, setVsEngine]               = useState(false);
  const [playerColor, setPlayerColor]         = useState<'WHITE' | 'BLACK'>('WHITE');
  const [engineElo, setEngineElo]             = useState(800);
  const [engineThinking, setEngineThinking]   = useState(false);
  const [isVsEngineGame, setIsVsEngineGame]   = useState(false);
  const [myColor, setMyColor]                 = useState<'WHITE' | 'BLACK'>('WHITE');
  const [lastEngineMove, setLastEngineMove]   = useState<string | null>(null);

  // move classification badge (player's last move)
  const [moveClassification, setMoveClassification] = useState<string | null>(null);
  const [classificationSquare, setClassificationSquare] = useState<string | null>(null);

  // evaluation bar
  const [evalScore, setEvalScore]   = useState(0);
  const [evalIsMate, setEvalIsMate] = useState(false);
  const [evalMateIn, setEvalMateIn] = useState(0);

  // clock
  const [timeControl, setTimeControl] = useState(0);         // seconds (0 = unlimited)
  const [timeWhite, setTimeWhite]     = useState(0);
  const [timeBlack, setTimeBlack]     = useState(0);
  const [flagged, setFlagged]         = useState<'WHITE' | 'BLACK' | null>(null);
  const clockRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const playSound = useChessSound();

  // Start / stop the clock whenever turn or game status changes.
  // While the engine is "thinking" (frontend delay), tick the engine's clock
  // instead of the player's — the player already moved.
  useEffect(() => {
    if (clockRef.current) clearInterval(clockRef.current);
    if (timeControl === 0 || status !== 'IN_PROGRESS' || flagged) return;

    const engineColor = myColor === 'WHITE' ? 'BLACK' : 'WHITE';
    const tickingColor = engineThinking ? engineColor : turn;

    clockRef.current = setInterval(() => {
      if (tickingColor === 'WHITE') {
        setTimeWhite(t => {
          if (t <= 1) { clearInterval(clockRef.current!); setFlagged('WHITE'); return 0; }
          return t - 1;
        });
      } else {
        setTimeBlack(t => {
          if (t <= 1) { clearInterval(clockRef.current!); setFlagged('BLACK'); return 0; }
          return t - 1;
        });
      }
    }, 1000);

    return () => { if (clockRef.current) clearInterval(clockRef.current); };
  }, [turn, status, engineThinking, timeControl, flagged, myColor]);

  useEffect(() => { startNewGame(); }, []);

  const refreshEval = (position: string) => {
    analyzeRest({ fen: position, depth: 14 })
      .then(r => { setEvalScore(r.score); setEvalIsMate(r.isMate); setEvalMateIn(r.mateIn); })
      .catch(() => {});
  };

  const startNewGame = async () => {
    if (clockRef.current) clearInterval(clockRef.current);
    setLoading(true);
    setError(null);
    setFlagged(null);
    setEvalScore(0); setEvalIsMate(false); setEvalMateIn(0);
    setTimeWhite(timeControl);
    setTimeBlack(timeControl);
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
      setLastEngineMove(null);
      setMoveClassification(null);
      setClassificationSquare(null);
      refreshEval(game.fen);
    } catch {
      setError('Failed to create game.');
    } finally {
      setLoading(false);
    }
  };

  const handleMove = useCallback(async (uciMove: string) => {
    if (!gameId || status !== 'IN_PROGRESS' || flagged) return;
    if (isVsEngineGame && turn !== myColor) return;
    setError(null);
    setEngineThinking(isVsEngineGame);
    const fenBeforeMove = fen;
    try {
      const res: MakeMoveResponse = await makeMove(gameId, uciMove);

      if (res.engineMove) {
        // Phase 1: show the player's move immediately, clear last engine highlight
        setLastEngineMove(null);
        setMoveClassification(null);
        setClassificationSquare(null);
        if (res.playerMoveFen) setFen(res.playerMoveFen);
        setMoveHistory(prev => [...prev, uciMove]);
        // Play sound for the player's move
        playSound(wasCapture(fenBeforeMove, res.playerMoveFen ?? res.fen) ? 'capture' : 'move');
        // Show the classification badge right away (player sees it during engine "think")
        if (res.moveClassification) {
          setClassificationSquare(uciMove.slice(2, 4));
          setMoveClassification(res.moveClassification);
        }

        // Phase 2: ELO-scaled delay before the engine move appears on the board.
        // Lower ELO = slightly faster (bad move was easy to find); still at least 2s.
        // Formula: 2000ms at 300 ELO, scaling up to 4500ms at 2500 ELO.
        const baseDelay = Math.max(2000, Math.min(4500, 2000 + engineElo));
        const delay = baseDelay + Math.random() * 400; // ±400ms natural variation
        await new Promise(resolve => setTimeout(resolve, delay));

        setFen(res.fen);
        setTurn(res.turn);
        setStatus(res.status);
        setLastEngineMove(res.engineMove);
        setMoveHistory(prev => [...prev, res.engineMove!]);
        // Play engine move sound
        playSound(wasCapture(res.playerMoveFen ?? fenBeforeMove, res.fen) ? 'capture' : 'move');
        if (res.status !== 'IN_PROGRESS') playSound('gameOver');
      } else {
        // Human vs human, engine unavailable, or game ended on the player's move
        setLastEngineMove(null);
        setFen(res.fen);
        setTurn(res.turn);
        setStatus(res.status);
        setMoveHistory(prev => [...prev, uciMove]);
        if (res.moveClassification) {
          setClassificationSquare(uciMove.slice(2, 4));
          setMoveClassification(res.moveClassification);
        } else {
          setMoveClassification(null);
          setClassificationSquare(null);
        }
        playSound(wasCapture(fenBeforeMove, res.fen) ? 'capture' : 'move');
        if (res.status !== 'IN_PROGRESS') playSound('gameOver');
      }

      refreshEval(res.fen);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail ?? 'Illegal move';
      setError(message);
    } finally {
      setEngineThinking(false);
    }
  }, [gameId, status, isVsEngineGame, turn, myColor, flagged, engineElo]);

  const boardDisabled = status !== 'IN_PROGRESS' || engineThinking || !!flagged
    || (isVsEngineGame && turn !== myColor);

  const handleResign = useCallback(async () => {
    if (!gameId || status !== 'IN_PROGRESS') return;
    if (!window.confirm('Are you sure you want to resign?')) return;
    try {
      const res = await resign(gameId);
      setStatus(res.status);
      playSound('gameOver');
    } catch { /* ignore */ }
  }, [gameId, status, playSound]);

  const handleDraw = useCallback(async () => {
    if (!gameId || status !== 'IN_PROGRESS') return;
    if (!window.confirm('Offer a draw and end the game?')) return;
    try {
      const res = await offerDraw(gameId);
      setStatus(res.status);
      playSound('gameOver');
    } catch { /* ignore */ }
  }, [gameId, status, playSound]);

  const statusMessage = (): string => {
    if (flagged)         return `${flagged === 'WHITE' ? 'White' : 'Black'} ran out of time!`;
    if (engineThinking)  return 'Engine is thinking…';
    switch (status) {
      case 'IN_PROGRESS': return `${turn === 'WHITE' ? 'White' : 'Black'} to move`;
      case 'CHECKMATE':   return 'Checkmate!';
      case 'STALEMATE':   return 'Stalemate – Draw';
      default:            return status.replace(/_/g, ' ');
    }
  };

  const clockEnabled = timeControl > 0;
  // Clocks are shown: opponent's clock on top, player's clock on bottom
  const topColor    = myColor === 'WHITE' ? 'BLACK' : 'WHITE';
  const bottomColor = myColor;

  return (
    <div className={styles.appShell}>
      <header className={styles.header}>
        <h1>♟ Chess App</h1>

        <div className={styles.modeSelector}>
          <label className={styles.modeToggle}>
            <input type="checkbox" checked={vsEngine} onChange={e => setVsEngine(e.target.checked)} />
            vs Engine
          </label>

          {vsEngine && (
            <>
              <select className={styles.modeSelect} value={playerColor}
                onChange={e => setPlayerColor(e.target.value as 'WHITE' | 'BLACK')}>
                <option value="WHITE">Play White</option>
                <option value="BLACK">Play Black</option>
              </select>
              <select className={styles.modeSelect} value={engineElo}
                onChange={e => setEngineElo(Number(e.target.value))}>
                {ELO_OPTIONS.map(elo => <option key={elo} value={elo}>{elo} ELO</option>)}
              </select>
            </>
          )}

          <select className={styles.modeSelect} value={timeControl}
            onChange={e => setTimeControl(Number(e.target.value))}>
            {TIME_CONTROLS.map(tc => <option key={tc.seconds} value={tc.seconds}>{tc.label}</option>)}
          </select>
        </div>

        <button className={styles.primary} onClick={startNewGame} disabled={loading}>
          {loading ? 'Loading…' : 'New Game'}
        </button>
      </header>

      <main className={styles.gameArea}>
        <section className={styles.boardSection}>
          <div className={`${styles.statusBar} ${status !== 'IN_PROGRESS' || flagged ? styles.gameOver : ''}`}>
            {statusMessage()}
          </div>

          {/* Opponent's clock */}
          {clockEnabled && (
            <div className={styles.clockRow}>
              <GameClock
                seconds={topColor === 'WHITE' ? timeWhite : timeBlack}
                active={turn === topColor && status === 'IN_PROGRESS' && !engineThinking}
                label={topColor === 'WHITE' ? 'White' : 'Black'}
                flagged={flagged === topColor}
              />
            </div>
          )}

          <div className={styles.boardWithBar}>
            <EvalBar score={evalScore} isMate={evalIsMate} mateIn={evalMateIn} />
            <ChessBoard
              fen={fen}
              disabled={boardDisabled}
              playerColor={isVsEngineGame ? myColor : undefined}
              lastMove={lastEngineMove}
              moveClassification={moveClassification}
              classificationSquare={classificationSquare}
              onMove={handleMove}
            />
          </div>

          {/* Player's clock */}
          {clockEnabled && (
            <div className={styles.clockRow}>
              <GameClock
                seconds={bottomColor === 'WHITE' ? timeWhite : timeBlack}
                active={turn === bottomColor && status === 'IN_PROGRESS' && !engineThinking}
                label={bottomColor === 'WHITE' ? 'White' : 'Black'}
                flagged={flagged === bottomColor}
              />
            </div>
          )}

          {error && <div className={styles.errorBanner}>{error}</div>}

          {status === 'IN_PROGRESS' && !flagged && (
            <div className={styles.gameActions}>
              <button className={styles.drawBtn} onClick={handleDraw} disabled={engineThinking}>
                ½ Offer Draw
              </button>
              <button className={styles.resignBtn} onClick={handleResign} disabled={engineThinking}>
                ⚑ Resign
              </button>
            </div>
          )}
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
