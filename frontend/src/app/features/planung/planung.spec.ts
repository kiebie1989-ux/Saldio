import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideEchartsCore } from 'ngx-echarts';
import { Planung } from './planung';

describe('Planung', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Planung],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideEchartsCore({ echarts: () => import('echarts') }),
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Jahresprognose und IST/PLAN-Zeilen', async () => {
    const fixture = TestBed.createComponent(Planung);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock
      .expectOne('/api/planung?mandant=Mustermann%20GmbH&jahr=2025&bisMonat=9&szenario=BASIS')
      .flush({
        mandant: 'Mustermann GmbH',
        jahr: 2025,
        bisMonat: 9,
        szenario: 'BASIS',
        zeilen: [
          { monat: '2025-01', typ: 'IST', umsatz: 92300, rohertrag: 61300, ebit: 22030 },
          { monat: '2025-10', typ: 'PLAN', umsatz: 99828.89, rohertrag: 66000, ebit: 21000 },
        ],
        jahresprognose: { umsatz: 1197946.67, rohertrag: 800000, ebit: 260000 },
      });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Jahresprognose');
    expect(text).toContain('PLAN');

    fixture.destroy();
  });
});
