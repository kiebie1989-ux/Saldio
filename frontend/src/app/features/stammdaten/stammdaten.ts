import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { AuthService } from '../../core/auth.service';
import { BwaApiService, Konto, Mandant, MandantEingabe, MitarbeiterDto } from '../../core/bwa-api.service';

function leererMandant(): MandantEingabe {
  return {
    name: '',
    status: 'AKTIV',
    imEinzelbericht: true,
    inKumulierung: true,
    imFinalbericht: false,
    typ: '',
    bemerkung: '',
    datevMandantennr: '',
    datevBeraternr: '',
  };
}

/** Stammdaten: Kontenrahmen/Mitarbeiter (read-only), Mandanten mit Pflege (Admin). */
@Component({
  selector: 'app-stammdaten',
  imports: [
    DecimalPipe,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatTabsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
  ],
  templateUrl: './stammdaten.html',
  styleUrl: './stammdaten.scss',
})
export class Stammdaten {
  private readonly api = inject(BwaApiService);
  protected readonly auth = inject(AuthService);

  protected readonly kontoSpalten = ['skr03', 'skr04', 'bezeichnung', 'bwaGruppe', 'guvBilanzPosition', 'aktiv'];
  protected readonly mandantSpalten = ['name', 'datevMandantennr', 'status', 'typ', 'inKumulierung', 'imFinalbericht', 'aktion'];
  protected readonly mitarbeiterSpalten = ['personalnummer', 'name', 'kostenstelle', 'monatslohn', 'euroProStunde', 'gesamtkosten'];

  protected readonly kontenrahmen = signal<Konto[]>([]);
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly mitarbeiter = signal<MitarbeiterDto[]>([]);

  // Mandanten-Formular (nur Admin)
  protected readonly formular = signal<MandantEingabe>(leererMandant());
  protected readonly editId = signal<number | null>(null);

  constructor() {
    this.api.getKontenrahmen().subscribe((k) => this.kontenrahmen.set(k));
    this.api.getMitarbeiter().subscribe((m) => this.mitarbeiter.set(m));
    this.ladeMandanten();
  }

  protected jaNein(b: boolean | undefined): string {
    return b ? 'Ja' : '–';
  }

  protected neuerMandant(): void {
    this.editId.set(null);
    this.formular.set(leererMandant());
  }

  protected bearbeite(m: Mandant): void {
    this.editId.set(m.id);
    this.formular.set({
      name: m.name,
      status: m.status,
      imEinzelbericht: !!m.imEinzelbericht,
      inKumulierung: !!m.inKumulierung,
      imFinalbericht: !!m.imFinalbericht,
      typ: m.typ ?? '',
      bemerkung: m.bemerkung ?? '',
      datevMandantennr: m.datevMandantennr ?? '',
      datevBeraternr: m.datevBeraternr ?? '',
    });
  }

  protected speichere(): void {
    const eingabe = this.formular();
    const id = this.editId();
    const obs = id == null ? this.api.createMandant(eingabe) : this.api.updateMandant(id, eingabe);
    obs.subscribe(() => {
      this.neuerMandant();
      this.ladeMandanten();
    });
  }

  private ladeMandanten(): void {
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
  }
}
