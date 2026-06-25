import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideEchartsCore } from 'ngx-echarts';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
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

  it('lädt Dashboard-KPIs und zeigt den Umsatz YTD', async () => {
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/dashboard?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      kpis: { umsatzYtd: 1239460, rohertragYtd: 820960, ebitYtd: 266611, ebitMarge: 21.5, mitarbeiter: 4 },
      monatsreihe: [{ monat: '2025-01', umsatz: 92300, rohertrag: 61300, ebit: 22030 }],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Umsatz YTD');
    expect(text).toContain('21.5 %');

    fixture.destroy();
  });
});
