import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { Ampel, BwaApiService, Mandant, Mandantenbericht } from '../../core/bwa-api.service';

/** KI-Auswertung: Tiefenanalyse je Bereich mit Ampel und Maßnahme (Excel-Blatt 07_KI_Auswertung). */
@Component({
  selector: 'app-ki-auswertung',
  imports: [MatCardModule, MatFormFieldModule, MatSelectModule, MatIconModule, MatChipsModule],
  templateUrl: './ki-auswertung.html',
  styleUrl: './ki-auswertung.scss',
})
export class KiAuswertung {
  private readonly api = inject(BwaApiService);

  protected readonly jahr = 2025;
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly bericht = signal<Mandantenbericht | null>(null);

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
