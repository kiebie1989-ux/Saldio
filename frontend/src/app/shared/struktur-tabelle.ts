import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { StrukturZeile } from '../core/bwa-api.service';

/**
 * Wiederverwendbare Tabelle für GuV/Bilanz: Positionen als Zeilen, Monate als dynamische Spalten,
 * plus YTD. Summen- und Prüfzeilen werden hervorgehoben.
 */
@Component({
  selector: 'app-struktur-tabelle',
  imports: [DecimalPipe, MatTableModule],
  templateUrl: './struktur-tabelle.html',
  styleUrl: './struktur-tabelle.scss',
})
export class StrukturTabelle {
  readonly monate = input.required<string[]>();
  readonly zeilen = input.required<StrukturZeile[]>();

  protected readonly spalten = computed(() => ['position', ...this.monate(), 'ytd']);
}
