import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateGameResponse, GameState, MakeMoveResponse } from '../models/game.model';

/**
 * Service for chess game REST operations.
 * Communicates exclusively through the backend's frontend-agnostic API.
 */
@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly baseUrl = '/api/games';

  constructor(private http: HttpClient) {}

  /** Creates a new game and returns its initial state. */
  createGame(): Observable<CreateGameResponse> {
    return this.http.post<CreateGameResponse>(this.baseUrl, {});
  }

  /** Retrieves the full state of a game by ID. */
  getGame(gameId: string): Observable<GameState> {
    return this.http.get<GameState>(`${this.baseUrl}/${gameId}`);
  }

  /** Submits a move in UCI notation (e.g. "e2e4"). */
  makeMove(gameId: string, uciMove: string): Observable<MakeMoveResponse> {
    return this.http.post<MakeMoveResponse>(`${this.baseUrl}/${gameId}/moves`, { move: uciMove });
  }
}
