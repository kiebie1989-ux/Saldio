import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BenutzerDto, BwaApiService, Mandant } from '../../core/bwa-api.service';

/** Admin-Seite: Benutzern Mandanten zuweisen (Mandanten-Datentrennung). */
@Component({
  selector: 'app-benutzer',
  imports: [
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatSelectModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './benutzer.html',
  styleUrl: './benutzer.scss',
})
export class Benutzer {
  private readonly api = inject(BwaApiService);

  protected readonly spalten = ['benutzername', 'alleMandanten', 'mandanten', 'aktion'];
  protected readonly benutzer = signal<BenutzerDto[]>([]);
  protected readonly alleMandanten = signal<Mandant[]>([]);
  protected readonly gespeichert = signal<string | null>(null);

  constructor() {
    this.api.getMandanten().subscribe((m) => this.alleMandanten.set(m));
    this.lade();
  }

  protected speichere(b: BenutzerDto): void {
    this.api.updateBenutzer(b.sub, b.alleMandanten, b.mandanten).subscribe((aktualisiert) => {
      this.gespeichert.set(aktualisiert.sub);
      setTimeout(() => this.gespeichert.set(null), 2000);
    });
  }

  private lade(): void {
    this.api.getBenutzer().subscribe((b) => this.benutzer.set(b));
  }
}
