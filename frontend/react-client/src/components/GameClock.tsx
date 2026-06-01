import React from 'react';
import styles from './GameClock.module.css';

interface GameClockProps {
  seconds: number;
  active: boolean;
  label: string;        // "White" | "Black"
  flagged: boolean;     // time ran out
}

function formatTime(totalSeconds: number): string {
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

const GameClock: React.FC<GameClockProps> = ({ seconds, active, label, flagged }) => (
  <div className={[
    styles.clock,
    active  ? styles.active  : '',
    flagged ? styles.flagged : '',
  ].join(' ').trim()}>
    <span className={styles.label}>{label}</span>
    <span className={styles.time}>{formatTime(seconds)}</span>
  </div>
);

export default GameClock;
