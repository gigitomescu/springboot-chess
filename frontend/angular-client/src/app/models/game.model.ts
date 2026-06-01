export interface GameState {
  gameId: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  moves: string[];
  moveCount: number;
  createdAt: string;
}

export interface CreateGameResponse {
  gameId: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  createdAt: string;
}

export interface MakeMoveResponse {
  gameId: string;
  move: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  gameOver: boolean;
}

export type GameStatus =
  | 'IN_PROGRESS'
  | 'CHECKMATE'
  | 'STALEMATE'
  | 'DRAW_FIFTY_MOVES'
  | 'DRAW_INSUFFICIENT_MATERIAL'
  | 'DRAW_THREEFOLD_REPETITION'
  | 'RESIGNED'
  | 'TIMEOUT';

/** A parsed 8×8 board where null = empty square. */
export type Board = (string | null)[][];
