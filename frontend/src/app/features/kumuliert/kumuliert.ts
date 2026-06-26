import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { BwaApiService, KumuliertBericht, Modus } from '../../core/bwa-api.service';
import { STANDARD_JAHR, VERFUEGBARE_JAHRE } from '../../core/jahre';

/** Kumuliert/Final (Excel-Blatt 08): Mandantenvergleich je Berichtsmodus. */
@Component({
  selector: 'app-kumuliert',
  imports: [DecimalPipe, MatCardModule, MatTableModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './kumuliert.html',
  styleUrl: './kumuliert.scss',
})
export class Kumuliert {
  private readonly api = inject(BwaApiService);

  protected readonly jahre = VERFUEGBARE_JAHRE;
  protected readonly jahr = signal(STANDARD_JAHR);
  protected readonly modusOptionen: Modus[] = ['EINZELN', 'KUMULIERT', 'FINAL'];
  protected readonly spalten = ['mandant', 'umsatz', 'rohertrag', 'rohertragsquote', 'ebit', 'ebitMarge'];
  protected readonly modus = signal<Modus>('KUMULIERT');
  protected readonly bericht = signal<KumuliertBericht | null>(null);

  constructor() {
    this.lade();
  }

  protected onModusChange(modus: Modus): void {
    this.modus.set(modus);
    this.lade();
  }

  protected onJahrChange(jahr: number): void {
    this.jahr.set(jahr);
    this.lade();
  }

  private lade(): void {
    this.api.getKumuliert(this.modus(), this.jahr()).subscribe((b) => this.bericht.set(b));
  }
}
