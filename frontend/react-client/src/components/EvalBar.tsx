import React from 'react';
import styles from './EvalBar.module.css';

interface EvalBarProps {
  /** Centipawns; positive = White is better, negative = Black is better. */
  score: number;
  isMate: boolean;
  /** Positive = White mates in N, negative = Black mates in N. */
  mateIn: number;
}

/** Converts centipawns to a 0–100 percentage of the bar that is white. */
const cpToWhitePct = (cp: number): number =>
  50 + 50 * (2 / Math.PI) * Math.atan(cp / 400);

const EvalBar: React.FC<EvalBarProps> = ({ score, isMate, mateIn }) => {
  const whitePct = isMate
    ? (mateIn > 0 ? 100 : 0)
    : Math.min(100, Math.max(0, cpToWhitePct(score)));

  const label = isMate
    ? `M${Math.abs(mateIn)}`
    : `${score > 0 ? '+' : ''}${(score / 100).toFixed(1)}`;

  return (
    <div className={styles.wrapper} title={`Evaluation: ${label}`}>
      <div className={styles.bar}>
        <div className={styles.blackSection} style={{ height: `${100 - whitePct}%` }} />
        <div className={styles.whiteSection} style={{ height: `${whitePct}%` }} />
      </div>
      <span className={styles.label}>{label}</span>
    </div>
  );
};

export default EvalBar;
