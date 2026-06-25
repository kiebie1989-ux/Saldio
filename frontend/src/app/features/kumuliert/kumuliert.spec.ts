import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Kumuliert } from './kumuliert';

describe('Kumuliert', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Kumuliert],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt den Mandantenvergleich mit Summe', async () => {
    const fixture = TestBed.createComponent(Kumuliert);
    fixture.detectChanges();

    httpMock.expectOne('/api/kumuliert?modus=KUMULIERT&jahr=2025').flush({
      modus: 'KUMULIERT',
      jahr: 2025,
      mandanten: [
        { mandant: 'Mustermann GmbH', umsatz: 1239460, rohertrag: 820960, rohertragsquote: 66.2, ebit: 266611, ebitMarge: 21.5 },
        { mandant: 'Beispiel Handel GmbH', umsatz: 102000, rohertrag: 40800, rohertragsquote: 40, ebit: 12800, ebitMarge: 12.5 },
      ],
      summe: { mandant: 'GESAMT (KUMULIERT)', umsatz: 1341460, rohertrag: 861760, rohertragsquote: 64.2, ebit: 279411, ebitMarge: 20.8 },
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Mustermann GmbH');
    expect(text).toContain('GESAMT (KUMULIERT)');
  });
});
