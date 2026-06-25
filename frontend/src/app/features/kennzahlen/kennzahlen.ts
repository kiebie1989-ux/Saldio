import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { BwaApiService, Ampel, Kennzahl, Mandant } from '../../core/bwa-api.service';

/** Kennzahlen-Übersicht mit Ampelbewertung (Excel-Blatt 05_Kennzahlen). */
@Component({
  selector: 'app-kennzahlen',
  imports: [
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
  ],
  templateUrl: './kennzahlen.html',
  styleUrl: './kennzahlen.scss',
})
export class Kennzahlen {
  private readonly api = inject(BwaApiService);

  protected readonly jahr = 2025;
  protected readonly spalten = ['name', 'wert', 'zielwert', 'ampel', 'interpretation'];
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly kennzahlen = signal<Kennzahl[]>([]);

  constructor() {
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
    this.lade();
  }

  protected onMandantChange(name: string): void {
    this.selectedMandant.set(name);
    this.lade();
  }

  protected ampelIcon(a: Ampel): string {
    return a === 'GRUEN' ? 'check_circle' : a === 'GELB' ? 'warning' : a === 'ROT' ? 'error' : 'remove';
  }

  protected ampelClass(a: Ampel): string {
    return 'ampel-' + a.toLowerCase();
  }

  private lade(): void {
    this.api.getKennzahlen(this.selectedMandant(), this.jahr).subscribe((k) => this.kennzahlen.set(k));
  }
}
