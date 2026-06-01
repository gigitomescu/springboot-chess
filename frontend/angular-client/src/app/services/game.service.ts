import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateGameRequest, CreateGameResponse, GameState, MakeMoveResponse } from '../models/game.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly baseUrl = '/api/games';

  constructor(private http: HttpClient) {}

  createGame(request: CreateGameRequest = {}): Observable<CreateGameResponse> {
    return this.http.post<CreateGameResponse>(this.baseUrl, request);
  }

  getGame(gameId: string): Observable<GameState> {
    return this.http.get<GameState>(`${this.baseUrl}/${gameId}`);
  }

  makeMove(gameId: string, uciMove: string): Observable<MakeMoveResponse> {
    return this.http.post<MakeMoveResponse>(`${this.baseUrl}/${gameId}/moves`, { move: uciMove });
  }

  resign(gameId: string): Observable<GameState> {
    return this.http.post<GameState>(`${this.baseUrl}/${gameId}/resign`, {});
  }

  offerDraw(gameId: string): Observable<GameState> {
    return this.http.post<GameState>(`${this.baseUrl}/${gameId}/draw`, {});
  }
}
