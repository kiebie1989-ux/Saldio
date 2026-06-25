import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Guv } from './guv';

describe('Guv', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Guv],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lädt die GuV-Struktur und zeigt Positionen', async () => {
    const fixture = TestBed.createComponent(Guv);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/guv?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      monate: ['2025-01'],
      zeilen: [
        { position: 'Umsatzerlöse', art: 'WERT', monate: { '2025-01': 92300 }, ytd: 1239460 },
        { position: '= Rohertrag (DB I)', art: 'SUMME', monate: { '2025-01': 61300 }, ytd: 820960 },
      ],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Umsatzerlöse');
    expect(text).toContain('Rohertrag (DB I)');
  });
});
