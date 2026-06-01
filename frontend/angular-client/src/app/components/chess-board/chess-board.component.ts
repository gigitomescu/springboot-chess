import {
  Component, Input, Output, EventEmitter, OnChanges, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Board } from '../../models/game.model';

/**
 * Renders an interactive 8×8 chess board from a FEN string.
 * Emits move events in UCI notation for the parent to submit.
 */
@Component({
  selector: 'app-chess-board',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chess-board.component.html',
  styleUrls: ['./chess-board.component.css']
})
export class ChessBoardComponent implements OnChanges {

  @Input() fen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
  @Input() playerColor: 'WHITE' | 'BLACK' = 'WHITE';
  @Input() disabled = false;

  @Output() moveMade = new EventEmitter<string>();

  board: Board = [];
  selectedSquare: string | null = null;
  dragFromSquare: string | null = null;
  dragOverSquare: string | null = null;
  files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
  ranks = [8, 7, 6, 5, 4, 3, 2, 1];

  /** Unicode chess piece symbols keyed by piece character. */
  readonly pieceSymbols: Record<string, string> = {
    K: '♔', Q: '♕', R: '♖', B: '♗', N: '♘', P: '♙',
    k: '♚', q: '♛', r: '♜', b: '♝', n: '♞', p: '♟'
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fen']) {
      this.board = this.parseFen(this.fen);
      this.selectedSquare = null;
    }
  }

  /** Returns the piece character for a given rank/file (0-indexed from top). */
  getPiece(rankIdx: number, fileIdx: number): string | null {
    return this.board[rankIdx]?.[fileIdx] ?? null;
  }

  /** Determines the algebraic square name from board indices. */
  squareName(rankIdx: number, fileIdx: number): string {
    const file = this.files[fileIdx];
    const rank = this.ranks[rankIdx];
    return `${file}${rank}`;
  }

  isLight(rankIdx: number, fileIdx: number): boolean {
    return (rankIdx + fileIdx) % 2 === 0;
  }

  isSelected(rankIdx: number, fileIdx: number): boolean {
    return this.selectedSquare === this.squareName(rankIdx, fileIdx);
  }

  isDragOver(rankIdx: number, fileIdx: number): boolean {
    return this.dragOverSquare === this.squareName(rankIdx, fileIdx);
  }

  /** Returns own piece on the square or null (respects playerColor restriction). */
  private getOwnPiece(rankIdx: number, fileIdx: number): string | null {
    const piece = this.getPiece(rankIdx, fileIdx);
    if (!piece) return null;
    const isWhitePiece = piece === piece.toUpperCase();
    if (this.playerColor === 'WHITE' && !isWhitePiece) return null;
    if (this.playerColor === 'BLACK' &&  isWhitePiece) return null;
    return piece;
  }

  canDrag(rankIdx: number, fileIdx: number): boolean {
    return !this.disabled && !!this.getOwnPiece(rankIdx, fileIdx);
  }

  onDragStart(rankIdx: number, fileIdx: number, event: DragEvent): void {
    if (!this.canDrag(rankIdx, fileIdx)) { event.preventDefault(); return; }
    const square = this.squareName(rankIdx, fileIdx);
    this.dragFromSquare = square;
    this.selectedSquare = square;
    event.dataTransfer!.effectAllowed = 'move';
    event.dataTransfer!.setData('text/plain', square);
  }

  onDragOver(rankIdx: number, fileIdx: number, event: DragEvent): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dragOverSquare = this.squareName(rankIdx, fileIdx);
  }

  onDragLeave(): void {
    this.dragOverSquare = null;
  }

  onDrop(rankIdx: number, fileIdx: number, event: DragEvent): void {
    event.preventDefault();
    const from = this.dragFromSquare;
    const to   = this.squareName(rankIdx, fileIdx);
    if (from && from !== to) this.moveMade.emit(from + to);
    this.dragFromSquare = null;
    this.dragOverSquare = null;
    this.selectedSquare = null;
  }

  onDragEnd(): void {
    this.dragFromSquare = null;
    this.dragOverSquare = null;
    this.selectedSquare = null;
  }

  onSquareClick(rankIdx: number, fileIdx: number): void {
    if (this.disabled) return;

    const square = this.squareName(rankIdx, fileIdx);
    const piece  = this.getPiece(rankIdx, fileIdx);

    if (this.selectedSquare === null) {
      // Select if there is a piece on this square
      if (piece) this.selectedSquare = square;
    } else {
      if (this.selectedSquare === square) {
        // Deselect on second click of same square
        this.selectedSquare = null;
      } else {
        // Emit the UCI move and deselect
        const uci = this.selectedSquare + square;
        this.moveMade.emit(uci);
        this.selectedSquare = null;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // FEN parsing
  // ---------------------------------------------------------------------------

  private parseFen(fen: string): Board {
    const position = fen.split(' ')[0];
    const rows     = position.split('/');
    return rows.map(row => {
      const cells: (string | null)[] = [];
      for (const ch of row) {
        const n = parseInt(ch, 10);
        if (isNaN(n)) {
          cells.push(ch);
        } else {
          for (let i = 0; i < n; i++) cells.push(null);
        }
      }
      return cells;
    });
  }
}
