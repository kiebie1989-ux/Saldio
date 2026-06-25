import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { BwaApiService, Mandant, StrukturBericht } from '../../core/bwa-api.service';
import { StrukturTabelle } from '../../shared/struktur-tabelle';

/** Gewinn- und Verlustrechnung (Excel-Blatt GuV_Struktur). */
@Component({
  selector: 'app-guv',
  imports: [MatCardModule, MatFormFieldModule, MatSelectModule, StrukturTabelle],
  templateUrl: './guv.html',
  styleUrl: './struktur-seite.scss',
})
export class Guv {
  private readonly api = inject(BwaApiService);

  protected readonly jahr = 2025;
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly bericht = signal<StrukturBericht | null>(null);

  constructor() {
    this.api.getMandanten().subscribe((m) => {
      this.mandanten.set(m);
      if (m.length) {
        if (!m.some((x) => x.name === this.selectedMandant())) {
          this.selectedMandant.set(m[0].name);
        }
        this.lade();
      }
    });
  }

  protected onMandantChange(name: string): void {
    this.selectedMandant.set(name);
    this.lade();
  }

  private lade(): void {
    this.api.getGuv(this.selectedMandant(), this.jahr).subscribe((b) => this.bericht.set(b));
  }
}
