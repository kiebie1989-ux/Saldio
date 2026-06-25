import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

/**
 * Generischer Platzhalter für noch nicht implementierte Feature-Seiten.
 * Zeigt den Feature-Namen aus den Routen-Daten an.
 */
@Component({
  selector: 'app-placeholder',
  imports: [MatCardModule, MatIconModule],
  template: `
    <mat-card>
      <mat-card-content class="placeholder">
        <mat-icon>construction</mat-icon>
        <div>
          <h2>{{ label }}</h2>
          <p>Diese Ansicht wird in einer späteren Roadmap-Phase umgesetzt.</p>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      .placeholder {
        display: flex;
        align-items: center;
        gap: 1rem;
      }
      mat-icon {
        font-size: 2.5rem;
        width: 2.5rem;
        height: 2.5rem;
        color: var(--mat-sys-outline);
      }
    `,
  ],
})
export class Placeholder {
  private readonly route = inject(ActivatedRoute);
  protected readonly label = this.route.snapshot.data['label'] ?? 'In Arbeit';
}
