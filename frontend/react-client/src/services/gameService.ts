import axios from 'axios';
import { CreateGameResponse, MakeMoveResponse, GameState } from '../types/chess.types';

const BASE_URL = '/api/games';

/** Creates a new chess game. */
export async function createGame(): Promise<CreateGameResponse> {
  const res = await axios.post<CreateGameResponse>(BASE_URL, {});
  return res.data;
}

/** Fetches the current state of a game. */
export async function getGame(gameId: string): Promise<GameState> {
  const res = await axios.get<GameState>(`${BASE_URL}/${gameId}`);
  return res.data;
}

/** Submits a move in UCI notation (e.g. "e2e4"). */
export async function makeMove(gameId: string, uciMove: string): Promise<MakeMoveResponse> {
  const res = await axios.post<MakeMoveResponse>(`${BASE_URL}/${gameId}/moves`, { move: uciMove });
  return res.data;
}
