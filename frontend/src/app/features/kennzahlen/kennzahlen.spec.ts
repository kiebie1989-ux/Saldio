import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Kennzahlen } from './kennzahlen';

describe('Kennzahlen', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Kennzahlen],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Kennzahlen mit Ampelbewertung', async () => {
    const fixture = TestBed.createComponent(Kennzahlen);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/kennzahlen?mandant=Mustermann%20GmbH&jahr=2025').flush([
      { name: 'EBIT-Marge', wert: 21.5, einheit: '%', zielwert: 18, ampel: 'GRUEN', richtung: 'HOEHER_BESSER', interpretation: 'Op. Ergebnis / Umsatz.' },
      { name: 'Sonstigenquote', wert: 22.5, einheit: '%', zielwert: 20, ampel: 'ROT', richtung: 'NIEDRIGER_BESSER', interpretation: 'Sonstige / Umsatz.' },
    ]);

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('EBIT-Marge');
    expect(text).toContain('Sonstigenquote');
  });
});
