import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AnalysisService } from '../../services/analysis.service';
import { AnalysisResponse } from '../../models/analysis.model';

/**
 * Displays Stockfish evaluation and best-move suggestion for the current
 * board position. Supports both REST (on-demand) and WebSocket (live) modes.
 */
@Component({
  selector: 'app-analysis-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analysis-panel.component.html',
  styleUrls: ['./analysis-panel.component.css']
})
export class AnalysisPanelComponent implements OnChanges, OnDestroy {

  @Input() fen = '';
  @Input() autoAnalyze = false;

  depth = 12;
  result: AnalysisResponse | null = null;
  loading = false;
  error: string | null = null;

  private wsSub?: Subscription;

  constructor(private analysisService: AnalysisService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fen'] && this.autoAnalyze && this.fen) {
      this.analyze();
    }
  }

  analyze(): void {
    this.loading = true;
    this.error   = null;

    this.analysisService.analyzeRest({ fen: this.fen, depth: this.depth })
      .subscribe({
        next: res => {
          this.result  = res;
          this.loading = false;
        },
        error: err => {
          this.error   = err.error?.detail ?? 'Analysis failed';
          this.loading = false;
        }
      });
  }

  /** Formatted score string shown to the user. */
  get formattedScore(): string {
    if (!this.result) return '–';
    if (this.result.isMate) {
      return this.result.mateIn > 0
        ? `M${this.result.mateIn}`
        : `-M${Math.abs(this.result.mateIn)}`;
    }
    const pawns = (this.result.score / 100).toFixed(2);
    return this.result.score >= 0 ? `+${pawns}` : pawns;
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }
}
