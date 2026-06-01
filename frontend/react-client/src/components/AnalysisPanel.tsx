import React, { useEffect, useState } from 'react';
import styles from './AnalysisPanel.module.css';
import { analyzeRest } from '../services/analysisService';
import { AnalysisResponse } from '../types/chess.types';

interface AnalysisPanelProps {
  fen: string;
  autoAnalyze?: boolean;
}

/**
 * Sidebar panel that shows Stockfish evaluation for the current position.
 */
const AnalysisPanel: React.FC<AnalysisPanelProps> = ({ fen, autoAnalyze = false }) => {
  const [depth, setDepth]     = useState(12);
  const [result, setResult]   = useState<AnalysisResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  useEffect(() => {
    if (autoAnalyze && fen) analyze();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fen, autoAnalyze]);

  const analyze = async () => {
    if (!fen) return;
    setLoading(true);
    setError(null);
    try {
      const res = await analyzeRest({ fen, depth });
      setResult(res);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail ?? 'Analysis failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const formattedScore = (): string => {
    if (!result) return '–';
    if (result.isMate) {
      return result.mateIn > 0 ? `M${result.mateIn}` : `-M${Math.abs(result.mateIn)}`;
    }
    const pawns = (result.score / 100).toFixed(2);
    return result.score >= 0 ? `+${pawns}` : pawns;
  };

  return (
    <div className={styles.panel}>
      <h3>Engine Analysis</h3>

      <div className={styles.controls}>
        <label>
          Depth
          <input
            type="number"
            value={depth}
            min={1}
            max={30}
            onChange={e => setDepth(Number(e.target.value))}
          />
        </label>
        <button
          className={styles.primary}
          onClick={analyze}
          disabled={loading || !fen}
        >
          {loading ? 'Analysing…' : 'Analyse'}
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {result && (
        <div className={styles.result}>
          <div className={styles.row}>
            <span className={styles.label}>Evaluation</span>
            <span className={`${styles.score} ${result.score > 0 ? styles.positive : result.score < 0 ? styles.negative : ''}`}>
              {formattedScore()}
            </span>
          </div>
          <div className={styles.row}>
            <span className={styles.label}>Best Move</span>
            <span className={styles.bestMove}>{result.bestMove}</span>
          </div>
          <div className={styles.row}>
            <span className={styles.label}>Depth</span>
            <span>{result.depth}</span>
          </div>
          {result.topMoves.length > 0 && (
            <div className={styles.pv}>
              <span className={styles.label}>Principal Variation</span>
              <p className={styles.pvLine}>{result.topMoves[0].line.join(' ')}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AnalysisPanel;
