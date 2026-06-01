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
