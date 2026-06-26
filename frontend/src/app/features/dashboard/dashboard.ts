import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { BwaApiService, DashboardBericht, Mandant } from '../../core/bwa-api.service';
import { STANDARD_JAHR, VERFUEGBARE_JAHRE } from '../../core/jahre';

/** Dashboard mit echten KPI-Kacheln und Monatscharts aus der Engine (Excel-Blatt 04_Dashboard). */
@Component({
  selector: 'app-dashboard',
  imports: [
    FormsModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
    NgxEchartsDirective,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly api = inject(BwaApiService);

  protected readonly jahre = VERFUEGBARE_JAHRE;
  protected readonly jahr = signal(STANDARD_JAHR);
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly bericht = signal<DashboardBericht | null>(null);
  protected readonly laedt = signal(false);

  protected readonly chartOptions = computed<EChartsCoreOption>(() => {
    const b = this.bericht();
    const reihe = b?.monatsreihe ?? [];
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['Umsatz', 'Rohertrag', 'EBIT'] },
      grid: { left: 64, right: 16, top: 40, bottom: 32 },
      xAxis: { type: 'category', data: reihe.map((m) => m.monat.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: 'Umsatz', type: 'line', smooth: true, data: reihe.map((m) => m.umsatz) },
        { name: 'Rohertrag', type: 'line', smooth: true, data: reihe.map((m) => m.rohertrag) },
        { name: 'EBIT', type: 'bar', data: reihe.map((m) => m.ebit) },
      ],
    };
  });

  constructor() {
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
    this.ladeDashboard();
  }

  protected onMandantChange(name: string): void {
    this.selectedMandant.set(name);
    this.ladeDashboard();
  }

  protected onJahrChange(jahr: number): void {
    this.jahr.set(jahr);
    this.ladeDashboard();
  }

  protected euro(value: number | undefined): string {
    return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 })
      .format(value ?? 0);
  }

  private ladeDashboard(): void {
    this.laedt.set(true);
    this.api.getDashboard(this.selectedMandant(), this.jahr()).subscribe({
      next: (b) => {
        this.bericht.set(b);
        this.laedt.set(false);
      },
      error: () => this.laedt.set(false),
    });
  }
}
