import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { BwaApiService, KostenstrukturBericht, Mandant } from '../../core/bwa-api.service';
import { STANDARD_JAHR, VERFUEGBARE_JAHRE } from '../../core/jahre';

/** Kostenstruktur (Excel-Blatt 10): Kostenarten je Monat + Lohnsumme je Kostenstelle. */
@Component({
  selector: 'app-kostenstruktur',
  imports: [DecimalPipe, MatCardModule, MatTableModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './kostenstruktur.html',
  styleUrl: './kostenstruktur.scss',
})
export class Kostenstruktur {
  private readonly api = inject(BwaApiService);

  protected readonly jahre = VERFUEGBARE_JAHRE;
  protected readonly jahr = signal(STANDARD_JAHR);
  protected readonly artSpalten = ['monat', 'umsatz', 'wareneinsatz', 'weQuote', 'personal', 'persQuote', 'sonstige', 'sonsQuote', 'gesamtkostenquote'];
  protected readonly stelleSpalten = ['kostenstelle', 'personalkosten', 'anteil'];
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly bericht = signal<KostenstrukturBericht | null>(null);

  constructor() {
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
    this.lade();
  }

  protected onMandantChange(name: string): void {
    this.selectedMandant.set(name);
    this.lade();
  }

  protected onJahrChange(jahr: number): void {
    this.jahr.set(jahr);
    this.lade();
  }

  private lade(): void {
    this.api.getKostenstruktur(this.selectedMandant(), this.jahr()).subscribe((b) => this.bericht.set(b));
  }
}
