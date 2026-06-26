import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { BwaApiService, ImportErgebnis } from '../../core/bwa-api.service';

/** Import-Seite: DATEV-Datei (CSV/EXTF) hochladen und Import-Historie anzeigen. */
@Component({
  selector: 'app-import-seite',
  imports: [
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './import-seite.html',
  styleUrl: './import-seite.scss',
})
export class ImportSeite {
  private readonly api = inject(BwaApiService);

  protected readonly spalten = ['importiertAm', 'dateiname', 'quelle', 'zeilenGesamt', 'zeilenOk', 'zeilenWarnung', 'status', 'aktion'];
  protected readonly typ = signal<'csv' | 'extf'>('csv');
  protected readonly laedt = signal(false);
  protected readonly letztesErgebnis = signal<ImportErgebnis | null>(null);
  protected readonly fehler = signal<string | null>(null);
  protected readonly historie = signal<ImportErgebnis[]>([]);

  constructor() {
    this.ladeHistorie();
  }

  protected setTyp(typ: 'csv' | 'extf'): void {
    this.typ.set(typ);
  }

  protected onDatei(event: Event): void {
    const input = event.target as HTMLInputElement;
    const datei = input.files?.[0];
    if (!datei) {
      return;
    }
    this.laedt.set(true);
    this.fehler.set(null);
    this.api.importiere(this.typ(), datei).subscribe({
      next: (e) => {
        this.letztesErgebnis.set(e);
        this.laedt.set(false);
        this.ladeHistorie();
      },
      error: () => {
        this.fehler.set('Import fehlgeschlagen. Format/Typ prüfen.');
        this.laedt.set(false);
      },
    });
    input.value = '';
  }

  protected storniere(id: number): void {
    this.api.stornoImport(id).subscribe({
      next: () => this.ladeHistorie(),
      error: () => {},
    });
  }

  private ladeHistorie(): void {
    this.api.getImportHistorie().subscribe((h) => this.historie.set(h));
  }
}
