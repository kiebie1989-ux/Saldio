import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { BwaApiService, Mandant, PlanungBericht, Szenario } from '../../core/bwa-api.service';

/** Planung & Forecast (Excel-Blatt 09): IST + projizierte Planmonate je Szenario. */
@Component({
  selector: 'app-planung',
  imports: [DecimalPipe, MatCardModule, MatTableModule, MatFormFieldModule, MatSelectModule, NgxEchartsDirective],
  templateUrl: './planung.html',
  styleUrl: './planung.scss',
})
export class Planung {
  private readonly api = inject(BwaApiService);

  protected readonly jahr = 2025;
  protected readonly bisMonat = 9;
  protected readonly szenarien: Szenario[] = ['PESSIMISTISCH', 'BASIS', 'OPTIMISTISCH'];
  protected readonly spalten = ['monat', 'typ', 'umsatz', 'rohertrag', 'ebit'];
  protected readonly mandanten = signal<Mandant[]>([]);
  protected readonly selectedMandant = signal('Mustermann GmbH');
  protected readonly szenario = signal<Szenario>('BASIS');
  protected readonly bericht = signal<PlanungBericht | null>(null);

  protected readonly chartOptions = computed<EChartsCoreOption>(() => {
    const z = this.bericht()?.zeilen ?? [];
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['Umsatz (IST)', 'Umsatz (PLAN)'] },
      grid: { left: 64, right: 16, top: 40, bottom: 32 },
      xAxis: { type: 'category', data: z.map((r) => r.monat.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: 'Umsatz (IST)', type: 'bar', stack: 'u', data: z.map((r) => (r.typ === 'IST' ? r.umsatz : null)) },
        { name: 'Umsatz (PLAN)', type: 'bar', stack: 'u', itemStyle: { opacity: 0.5 }, data: z.map((r) => (r.typ === 'PLAN' ? r.umsatz : null)) },
      ],
    };
  });

  constructor() {
    this.api.getMandanten().subscribe((m) => this.mandanten.set(m));
    this.lade();
  }

  protected onMandantChange(name: string): void {
    this.selectedMandant.set(name);
    this.lade();
  }

  protected onSzenarioChange(s: Szenario): void {
    this.szenario.set(s);
    this.lade();
  }

  private lade(): void {
    this.api
      .getPlanung(this.selectedMandant(), this.jahr, this.bisMonat, this.szenario())
      .subscribe((b) => this.bericht.set(b));
  }
}
