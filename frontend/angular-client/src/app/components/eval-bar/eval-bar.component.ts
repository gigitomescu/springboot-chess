import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-eval-bar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="eval-wrapper" [title]="'Evaluation: ' + label">
      <div class="eval-bar">
        <div class="black-section" [style.height.%]="100 - whitePct"></div>
        <div class="white-section" [style.height.%]="whitePct"></div>
      </div>
      <span class="eval-label">{{ label }}</span>
    </div>
  `,
  styles: [`
    .eval-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 22px;
      flex-shrink: 0;
      align-self: stretch;
    }
    .eval-bar {
      flex: 1;
      width: 100%;
      display: flex;
      flex-direction: column;
      border-radius: 4px;
      overflow: hidden;
      border: 1px solid #3a3a5a;
      min-height: 200px;
    }
    .black-section {
      background: #1a1a2e;
      transition: height 0.5s ease;
    }
    .white-section {
      background: #f5f0e8;
      transition: height 0.5s ease;
    }
    .eval-label {
      margin-top: 6px;
      font-size: 10px;
      font-weight: 700;
      color: #c9b37f;
      white-space: nowrap;
    }
  `]
})
export class EvalBarComponent implements OnChanges {
  @Input() score  = 0;
  @Input() isMate = false;
  @Input() mateIn = 0;

  whitePct = 50;
  label    = '0.0';

  ngOnChanges(): void {
    this.whitePct = this.isMate
      ? (this.mateIn > 0 ? 100 : 0)
      : Math.min(100, Math.max(0, 50 + 50 * (2 / Math.PI) * Math.atan(this.score / 400)));

    this.label = this.isMate
      ? `M${Math.abs(this.mateIn)}`
      : `${this.score > 0 ? '+' : ''}${(this.score / 100).toFixed(1)}`;
  }
}
