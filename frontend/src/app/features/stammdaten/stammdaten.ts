import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { BwaApiService, Konto, Mandant, MitarbeiterDto } from '../../core/bwa-api.service';

/** Stammdaten-Übersicht: Kontenrahmen, Mandanten, Mitarbeiter (read-only). */
@Component({
  selector: 'app-stammdaten',
  imports: [DecimalPipe, MatCardModule, MatTableModule, MatTabsModule, MatIconModule],
  templateUrl: './stammdaten.html',
  styleUrl: './stammdaten.scss',
})
export class Stammdaten {
  private readonly api = inject(BwaApiService);

  protected readonly kontoSpalten = ['skr03', 'skr04', 'bezeichnung', 'bwaGruppe', 'guvBilanzPosition', 'aktiv'];
  protected readonly mandantSpalten = ['name', 'status', 'typ', 'inKumulierung', 'imFinalbericht', 'bemerkung'];
  protected readonly mitarbeiterSpalten = ['personalnummer', 'name', 'kostenstelle', 'monatslohn', 'euroProStunde', 'gesamtkosten'];

  protected readonly kontenrahmen = signal<Konto[]>([]);
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly mitarbeiter = signal<MitarbeiterDto[]>([]);

  constructor() {
    this.api.getKontenrahmen().subscribe((k) => this.kontenrahmen.set(k));
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
    this.api.getMitarbeiter().subscribe((m) => this.mitarbeiter.set(m));
  }

  protected jaNein(b: boolean | undefined): string {
    return b ? 'Ja' : '–';
  }
}
