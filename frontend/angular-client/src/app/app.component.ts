import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChessBoardComponent } from './components/chess-board/chess-board.component';
import { AnalysisPanelComponent } from './components/analysis-panel/analysis-panel.component';
import { EvalBarComponent } from './components/eval-bar/eval-bar.component';
import { AnalysisService } from './services/analysis.service';
import { GameService } from './services/game.service';
import { SoundService } from './services/sound.service';
import { GameStatus } from './models/game.model';

const ELO_OPTIONS = [300, 600, 800, 1000, 1200, 1500, 1800, 2000, 2500];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, ChessBoardComponent, AnalysisPanelComponent, EvalBarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Chess App';

  gameId: string | null  = null;
  currentFen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
  currentTurn: 'WHITE' | 'BLACK' = 'WHITE';
  gameStatus: GameStatus = 'IN_PROGRESS';
  moveHistory: string[]  = [];
  errorMessage: string | null = null;
  loading = false;
  boardDisabled = false;

  // vs-engine settings
  vsEngine    = false;
  playerColor: 'WHITE' | 'BLACK' = 'WHITE';
  engineElo   = 800;
  eloOptions  = ELO_OPTIONS;
  engineThinking = false;
  isVsEngineGame = false;
  myColor: 'WHITE' | 'BLACK' = 'WHITE';

  // evaluation bar
  evalScore  = 0;
  evalIsMate = false;
  evalMateIn = 0;

  constructor(
    private gameService:     GameService,
    private analysisService: AnalysisService,
    private soundService:    SoundService
  ) {}

  ngOnInit(): void {
    this.newGame();
  }

  newGame(): void {
    this.loading = true;
    this.errorMessage = null;
    this.evalScore = 0; this.evalIsMate = false; this.evalMateIn = 0;

    const request = this.vsEngine
      ? { vsEngine: true, playerColor: this.playerColor, engineElo: this.engineElo }
      : {};

    this.gameService.createGame(request).subscribe({
      next: res => {
        this.gameId         = res.gameId;
        this.currentFen     = res.fen;
        this.currentTurn    = res.turn;
        this.gameStatus     = res.status;
        this.moveHistory    = [];
        this.boardDisabled  = false;
        this.isVsEngineGame = res.vsEngine;
        this.myColor        = res.playerColor ?? 'WHITE';
        this.loading        = false;
        this.refreshEval(res.fen);
      },
      error: err => {
        this.errorMessage = 'Failed to create game: ' + (err.error?.detail ?? err.message);
        this.loading = false;
      }
    });
  }

  onMoveMade(uciMove: string): void {
    if (!this.gameId || this.gameStatus !== 'IN_PROGRESS') return;
    if (this.isVsEngineGame && this.currentTurn !== this.myColor) return;

    this.errorMessage = null;
    this.engineThinking = this.isVsEngineGame;
    this.boardDisabled  = true;
    const fenBefore = this.currentFen;

    this.gameService.makeMove(this.gameId, uciMove).subscribe({
      next: res => {
        // Play sound for the player's move
        const playerFen = (res as any).playerMoveFen ?? res.fen;
        this.soundService.play(this.soundService.wasCapture(fenBefore, playerFen) ? 'capture' : 'move');
        this.currentFen  = res.fen;
        this.currentTurn = res.turn;
        this.gameStatus  = res.status;
        this.moveHistory.push(uciMove);
        if (res.engineMove) {
          // Play engine response sound
          this.soundService.play(this.soundService.wasCapture(playerFen, res.fen) ? 'capture' : 'move');
          this.moveHistory.push(res.engineMove);
        }
        if (res.gameOver) this.soundService.play('gameOver');
        this.engineThinking = false;
        this.boardDisabled  = res.gameOver || (this.isVsEngineGame && res.turn !== this.myColor);
        this.refreshEval(res.fen);
      },
      error: err => {
        this.errorMessage   = err.error?.detail ?? 'Illegal move';
        this.engineThinking = false;
        this.boardDisabled  = false;
      }
    });
  }

  resign(): void {
    if (!this.gameId || this.gameStatus !== 'IN_PROGRESS') return;
    if (!confirm('Are you sure you want to resign?')) return;
    this.gameService.resign(this.gameId).subscribe({
      next: res => {
        this.gameStatus    = res.status;
        this.boardDisabled = true;
        this.soundService.play('gameOver');
      },
      error: err => { this.errorMessage = err.error?.detail ?? 'Could not resign'; }
    });
  }

  offerDraw(): void {
    if (!this.gameId || this.gameStatus !== 'IN_PROGRESS') return;
    if (!confirm('Offer a draw and end the game?')) return;
    this.gameService.offerDraw(this.gameId).subscribe({
      next: res => {
        this.gameStatus    = res.status;
        this.boardDisabled = true;
        this.soundService.play('gameOver');
      },
      error: err => { this.errorMessage = err.error?.detail ?? 'Could not offer draw'; }
    });
  }

  get statusMessage(): string {
    if (this.engineThinking) return 'Engine is thinking…';
    switch (this.gameStatus) {
      case 'IN_PROGRESS': return `${this.currentTurn === 'WHITE' ? 'White' : 'Black'} to move`;
      case 'CHECKMATE':   return 'Checkmate!';
      case 'STALEMATE':   return 'Stalemate – Draw';
      default:            return this.gameStatus.replace(/_/g, ' ');
    }
  }

  private refreshEval(fen: string): void {
    this.analysisService.analyzeRest({ fen, depth: 14 }).subscribe({
      next: r => { this.evalScore = r.score; this.evalIsMate = r.isMate; this.evalMateIn = r.mateIn; },
      error: () => { /* engine may not be available */ }
    });
  }
}
