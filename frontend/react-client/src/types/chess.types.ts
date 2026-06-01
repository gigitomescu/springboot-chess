export type GameStatus =
  | 'IN_PROGRESS'
  | 'CHECKMATE'
  | 'STALEMATE'
  | 'DRAW_FIFTY_MOVES'
  | 'DRAW_INSUFFICIENT_MATERIAL'
  | 'DRAW_THREEFOLD_REPETITION'
  | 'DRAW_AGREEMENT'
  | 'RESIGNED'
  | 'TIMEOUT';

export interface CreateGameResponse {
  gameId: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  createdAt: string;
  vsEngine: boolean;
  playerColor: 'WHITE' | 'BLACK' | null;
}

export interface MakeMoveResponse {
  gameId: string;
  move: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  gameOver: boolean;
  engineMove: string | null;
  /** FEN after the player's move but before the engine replied; null in human vs human. */
  playerMoveFen: string | null;
  /** Quality of the player's move; null when engine is unavailable. */
  moveClassification: 'BRILLIANT' | 'EXCELLENT' | 'GOOD' | 'INACCURACY' | 'MISTAKE' | 'BLUNDER' | null;
}

export interface GameState {
  gameId: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  moves: string[];
  moveCount: number;
  vsEngine: boolean;
  playerColor: 'WHITE' | 'BLACK' | null;
}

export interface AnalysisRequest {
  fen: string;
  depth: number;
}

export interface AnalysisResponse {
  fen: string;
  bestMove: string;
  score: number;
  isMate: boolean;
  mateIn: number;
  depth: number;
  topMoves: MoveEvaluation[];
}

export interface MoveEvaluation {
  move: string;
  score: number;
  isMate: boolean;
  mateIn: number;
  line: string[];
}

/** 8×8 grid; null = empty square, string = piece character. */
export type Board = (string | null)[][];
