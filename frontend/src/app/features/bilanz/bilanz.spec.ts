import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Bilanz } from './bilanz';

describe('Bilanz', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Bilanz],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lädt die Bilanz inkl. Bilanzdifferenz-Prüfzeile', async () => {
    const fixture = TestBed.createComponent(Bilanz);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/bilanz?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      monate: ['2025-01'],
      zeilen: [
        { position: '= BILANZSUMME AKTIVA', art: 'SUMME', monate: { '2025-01': 953000 }, ytd: 953000 },
        { position: 'Bilanzdifferenz (Soll = 0)', art: 'PRUEFUNG', monate: { '2025-01': 0 }, ytd: 0 },
      ],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('BILANZSUMME AKTIVA');
    expect(text).toContain('Bilanzdifferenz');
  });
});
