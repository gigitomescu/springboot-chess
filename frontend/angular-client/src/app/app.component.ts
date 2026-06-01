import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChessBoardComponent } from './components/chess-board/chess-board.component';
import { AnalysisPanelComponent } from './components/analysis-panel/analysis-panel.component';
import { GameService } from './services/game.service';
import { GameStatus } from './models/game.model';

/**
 * Root application component.
 * Orchestrates game creation, move submission, and analysis.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ChessBoardComponent, AnalysisPanelComponent],
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

  constructor(private gameService: GameService) {}

  ngOnInit(): void {
    this.newGame();
  }

  newGame(): void {
    this.loading = true;
    this.errorMessage = null;
    this.gameService.createGame().subscribe({
      next: res => {
        this.gameId       = res.gameId;
        this.currentFen   = res.fen;
        this.currentTurn  = res.turn;
        this.gameStatus   = res.status;
        this.moveHistory  = [];
        this.boardDisabled = false;
        this.loading      = false;
      },
      error: err => {
        this.errorMessage = 'Failed to create game: ' + (err.error?.detail ?? err.message);
        this.loading = false;
      }
    });
  }

  onMoveMade(uciMove: string): void {
    if (!this.gameId || this.gameStatus !== 'IN_PROGRESS') return;

    this.gameService.makeMove(this.gameId, uciMove).subscribe({
      next: res => {
        this.currentFen  = res.fen;
        this.currentTurn = res.turn;
        this.gameStatus  = res.status;
        this.moveHistory.push(uciMove);
        this.errorMessage = null;
        if (res.gameOver) {
          this.boardDisabled = true;
        }
      },
      error: err => {
        this.errorMessage = err.error?.detail ?? 'Illegal move';
      }
    });
  }

  get statusMessage(): string {
    switch (this.gameStatus) {
      case 'IN_PROGRESS': return `${this.currentTurn === 'WHITE' ? 'White' : 'Black'} to move`;
      case 'CHECKMATE':   return 'Checkmate!';
      case 'STALEMATE':   return 'Stalemate – Draw';
      default:            return this.gameStatus.replace(/_/g, ' ');
    }
  }
}
