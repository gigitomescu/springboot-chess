export type GameStatus =
  | 'IN_PROGRESS'
  | 'CHECKMATE'
  | 'STALEMATE'
  | 'DRAW_FIFTY_MOVES'
  | 'DRAW_INSUFFICIENT_MATERIAL'
  | 'DRAW_THREEFOLD_REPETITION'
  | 'RESIGNED'
  | 'TIMEOUT';

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

export interface GameState {
  gameId: string;
  fen: string;
  turn: 'WHITE' | 'BLACK';
  status: GameStatus;
  moves: string[];
  moveCount: number;
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
