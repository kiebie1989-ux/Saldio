import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BwaApiService, Einstellung } from '../../core/bwa-api.service';

/** Einstellungen: Parameter anzeigen und bearbeiten. Zielwerte steuern live die Ampel-Logik. */
@Component({
  selector: 'app-einstellungen',
  imports: [
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './einstellungen.html',
  styleUrl: './einstellungen.scss',
})
export class Einstellungen {
  private readonly api = inject(BwaApiService);

  protected readonly spalten = ['schluessel', 'wert', 'beschreibung', 'aktion'];
  protected readonly einstellungen = signal<Einstellung[]>([]);
  protected readonly gespeichert = signal<string | null>(null);

  constructor() {
    this.lade();
  }

  protected speichere(e: Einstellung): void {
    this.api.updateEinstellung(e.schluessel, e.wert).subscribe((aktualisiert) => {
      this.gespeichert.set(aktualisiert.schluessel);
      setTimeout(() => this.gespeichert.set(null), 2000);
    });
  }

  private lade(): void {
    this.api.getEinstellungen().subscribe((e) => this.einstellungen.set(e));
  }
}
