import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ImportSeite } from './import-seite';

describe('ImportSeite', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportSeite],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt die Import-Historie', async () => {
    const fixture = TestBed.createComponent(ImportSeite);
    fixture.detectChanges();

    httpMock.expectOne('/api/import').flush([
      { id: 1, dateiname: 'saldenliste.csv', quelle: 'CSV', importiertAm: '2026-06-25T10:00:00+02:00', zeilenGesamt: 122, zeilenOk: 122, zeilenWarnung: 0, status: 'OK' },
    ]);

    await fixture.whenStable();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('saldenliste.csv');
    expect(text).toContain('DATEV-Datei importieren');
  });

  it('lädt eine Datei hoch und zeigt das Ergebnis', async () => {
    const fixture = TestBed.createComponent(ImportSeite);
    fixture.detectChanges();
    httpMock.expectOne('/api/import').flush([]);

    const datei = new File(['Monat;Mandant'], 'test.csv', { type: 'text/csv' });
    (fixture.componentInstance as unknown as { onDatei(e: unknown): void }).onDatei({
      target: { files: [datei], value: '' },
    });

    const post = httpMock.expectOne('/api/import');
    expect(post.request.method).toBe('POST');
    post.flush({ id: 2, dateiname: 'test.csv', quelle: 'CSV', importiertAm: 'x', zeilenGesamt: 1, zeilenOk: 1, zeilenWarnung: 0, status: 'OK' });
    httpMock.expectOne('/api/import').flush([]); // Historie-Reload nach Upload

    await fixture.whenStable();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('test.csv');
  });
});
