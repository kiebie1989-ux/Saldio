import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Kostenstruktur } from './kostenstruktur';

describe('Kostenstruktur', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Kostenstruktur],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Kostenarten und Kostenstellen', async () => {
    const fixture = TestBed.createComponent(Kostenstruktur);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/kostenstruktur?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      kostenarten: [{ monat: '2025-01', umsatz: 92300, wareneinsatz: 31000, weQuote: 33.6, personal: 22000, persQuote: 23.8, sonstige: 17270, sonsQuote: 18.7, gesamtkosten: 70270, gesamtkostenquote: 76.1 }],
      kostenstellen: [{ kostenstelle: 'IT', personalkosten: 9500, anteil: 46.6 }],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('2025-01');
    expect(text).toContain('IT');
  });
});
