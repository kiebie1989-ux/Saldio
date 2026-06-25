import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Einstellungen } from './einstellungen';

describe('Einstellungen', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Einstellungen],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Parameter und speichert eine Änderung', async () => {
    const fixture = TestBed.createComponent(Einstellungen);
    fixture.detectChanges();

    httpMock.expectOne('/api/einstellungen').flush([
      { schluessel: 'Ziel-EBIT-Marge %', wert: '18', beschreibung: 'Schwellwert EBIT' },
    ]);

    await fixture.whenStable();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Ziel-EBIT-Marge %');

    // Speichern löst PUT aus
    (fixture.componentInstance as unknown as { speichere(e: unknown): void }).speichere({
      schluessel: 'Ziel-EBIT-Marge %',
      wert: '25',
      beschreibung: 'Schwellwert EBIT',
    });
    const put = httpMock.expectOne('/api/einstellungen');
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual({ schluessel: 'Ziel-EBIT-Marge %', wert: '25' });
    put.flush({ schluessel: 'Ziel-EBIT-Marge %', wert: '25', beschreibung: 'Schwellwert EBIT' });
  });
});
