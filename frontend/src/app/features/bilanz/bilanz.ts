import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { BwaApiService, Mandant, StrukturBericht } from '../../core/bwa-api.service';
import { StrukturTabelle } from '../../shared/struktur-tabelle';

/** Bilanz (Excel-Blatt Bilanz_Struktur), inkl. Bilanzdifferenz-Prüfzeile. */
@Component({
  selector: 'app-bilanz',
  imports: [MatCardModule, MatFormFieldModule, MatSelectModule, StrukturTabelle],
  templateUrl: './bilanz.html',
  styleUrl: '../guv/struktur-seite.scss',
})
export class Bilanz {
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
    this.api.getBilanz(this.selectedMandant(), this.jahr).subscribe((b) => this.bericht.set(b));
  }
}
