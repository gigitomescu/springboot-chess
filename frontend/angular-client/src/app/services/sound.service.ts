import { Injectable } from '@angular/core';

export type ChessSoundType = 'move' | 'capture' | 'check' | 'gameOver';

// Singleton AudioContext — created lazily, survives across calls.
let _ctx: AudioContext | null = null;

function getCtx(): AudioContext {
  if (!_ctx || _ctx.state === 'closed') {
    _ctx = new AudioContext();
  }
  if (_ctx.state === 'suspended') void _ctx.resume();
  return _ctx;
}

function tone(
  ctx: AudioContext,
  startFreq: number,
  endFreq: number,
  duration: number,
  volume: number,
  type: OscillatorType = 'triangle',
  startAt = ctx.currentTime,
): void {
  const osc  = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.type = type;
  osc.frequency.setValueAtTime(startFreq, startAt);
  osc.frequency.exponentialRampToValueAtTime(endFreq, startAt + duration);
  gain.gain.setValueAtTime(volume, startAt);
  gain.gain.exponentialRampToValueAtTime(0.0001, startAt + duration);
  osc.connect(gain);
  gain.connect(ctx.destination);
  osc.start(startAt);
  osc.stop(startAt + duration + 0.01);
}

@Injectable({ providedIn: 'root' })
export class SoundService {
  play(type: ChessSoundType): void {
    try {
      const ctx = getCtx();
      const t   = ctx.currentTime;
      switch (type) {
        case 'move':
          tone(ctx, 900, 650, 0.07, 0.18, 'triangle', t);
          break;
        case 'capture':
          tone(ctx, 700, 160, 0.18, 0.30, 'sawtooth', t);
          tone(ctx, 500, 120, 0.18, 0.20, 'triangle', t);
          break;
        case 'check':
          tone(ctx, 1200, 1200, 0.07, 0.28, 'sine', t);
          tone(ctx, 1000, 1000, 0.07, 0.22, 'sine', t + 0.12);
          break;
        case 'gameOver':
          tone(ctx, 880, 880, 0.18, 0.28, 'sine', t);
          tone(ctx, 660, 660, 0.18, 0.28, 'sine', t + 0.22);
          tone(ctx, 440, 440, 0.28, 0.28, 'sine', t + 0.44);
          break;
      }
    } catch {
      // Audio blocked by browser — fail silently.
    }
  }

  /** Returns true when the FEN position has fewer pieces after the move (= capture). */
  wasCapture(fenBefore: string, fenAfter: string): boolean {
    const count = (fen: string): number =>
      fen.split(' ')[0].replace(/[1-8/]/g, '').length;
    return count(fenAfter) < count(fenBefore);
  }
}
