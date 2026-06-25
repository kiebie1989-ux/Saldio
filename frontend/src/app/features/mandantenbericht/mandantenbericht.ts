import { Component, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Ampel, BwaApiService, Mandant, Mandantenbericht as Bericht } from '../../core/bwa-api.service';

/** Executive-Mandantenbericht: Bereichsanalysen, Managementkommentar und PDF-Export. */
@Component({
  selector: 'app-mandantenbericht',
  imports: [
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './mandantenbericht.html',
  styleUrl: './mandantenbericht.scss',
})
export class Mandantenbericht {
  private readonly api = inject(BwaApiService);

  protected readonly jahr = 2025;
  protected readonly spalten = ['bereich', 'ampel', 'bewertung', 'massnahme', 'analyse'];
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly bericht = signal<Bericht | null>(null);

  protected readonly pdfHref = computed(() => this.api.pdfUrl(this.selectedMandant(), this.jahr));

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
    this.api.getMandantenbericht(this.selectedMandant(), this.jahr).subscribe((b) => this.bericht.set(b));
  }
}
