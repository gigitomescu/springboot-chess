import axios from 'axios';
import { CreateGameResponse, MakeMoveResponse, GameState } from '../types/chess.types';

const BASE_URL = '/api/games';

export interface CreateGameOptions {
  vsEngine?: boolean;
  playerColor?: 'WHITE' | 'BLACK';
  engineElo?: number;
}

/** Creates a new chess game. */
export async function createGame(options: CreateGameOptions = {}): Promise<CreateGameResponse> {
  const res = await axios.post<CreateGameResponse>(BASE_URL, {
    vsEngine:    options.vsEngine    ?? false,
    playerColor: options.playerColor ?? 'WHITE',
    engineElo:   options.engineElo   ?? null,
  });
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

/** Resigns the current player, ending the game immediately. */
export async function resign(gameId: string): Promise<GameState> {
  const res = await axios.post<GameState>(`${BASE_URL}/${gameId}/resign`);
  return res.data;
}

/** Accepts a draw, ending the game as a draw agreement. */
export async function offerDraw(gameId: string): Promise<GameState> {
  const res = await axios.post<GameState>(`${BASE_URL}/${gameId}/draw`);
  return res.data;
}
