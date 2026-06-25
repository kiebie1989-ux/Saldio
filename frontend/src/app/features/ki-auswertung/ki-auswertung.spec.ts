import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { KiAuswertung } from './ki-auswertung';

describe('KiAuswertung', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KiAuswertung],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Bereichskarten mit Maßnahme', async () => {
    const fixture = TestBed.createComponent(KiAuswertung);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/mandantenbericht?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      quelle: 'regelbasiert',
      bereiche: [
        { bereich: 'Sonstiges', ampel: 'ROT', bewertung: 'kritisch', massnahme: 'Ursachen analysieren', analyse: 'Sonstige zu hoch.' },
      ],
      managementkommentar: [],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Sonstiges');
    expect(text).toContain('Ursachen analysieren');
  });
});
