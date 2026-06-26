import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Stammdaten } from './stammdaten';
import { AuthService } from '../../core/auth.service';

const authStub: Partial<AuthService> = { hasRole: () => true };

describe('Stammdaten', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Stammdaten],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: AuthService, useValue: authStub },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lädt Kontenrahmen, Mandanten und Mitarbeiter', async () => {
    const fixture = TestBed.createComponent(Stammdaten);
    fixture.detectChanges();

    httpMock.expectOne('/api/kontenrahmen').flush([
      { id: 1, skr03: '8000', skr04: '4000', bezeichnung: 'Umsatzerlöse 19%', bwaGruppe: 'Umsatz', guvBilanzPosition: 'GuV: Umsatzerlöse', kontenklasse: 'Erlöse', vorzeichen: '+', aktiv: true },
    ]);
    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV', inKumulierung: true }]);
    httpMock.expectOne('/api/mitarbeiter').flush([
      { personalnummer: 'E001', name: 'Anna Schmidt', mandant: 'Mustermann GmbH', kostenstelle: 'IT', team: 'Dev', monatslohn: 5200, stundenProMonat: 160, euroProStunde: 32.5, agAnteil: 1040, gesamtkosten: 6240 },
    ]);

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Umsatzerlöse 19%');
  });
});
