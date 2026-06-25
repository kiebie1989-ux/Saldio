import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Mandantenbericht } from './mandantenbericht';

describe('Mandantenbericht', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mandantenbericht],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Bereichsanalyse und Managementkommentar', async () => {
    const fixture = TestBed.createComponent(Mandantenbericht);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([{ id: 1, name: 'Mustermann GmbH', status: 'AKTIV' }]);
    httpMock.expectOne('/api/mandantenbericht?mandant=Mustermann%20GmbH&jahr=2025').flush({
      mandant: 'Mustermann GmbH',
      jahr: 2025,
      quelle: 'regelbasiert',
      bereiche: [
        { bereich: 'EBIT', ampel: 'GRUEN', bewertung: 'im grünen Bereich', massnahme: 'Niveau halten', analyse: 'EBIT ok.' },
      ],
      managementkommentar: [{ titel: '1. Geschäftsentwicklung', text: 'Umsatz solide.' }],
    });

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('regelbasiert');
    expect(text).toContain('1. Geschäftsentwicklung');
  });
});
