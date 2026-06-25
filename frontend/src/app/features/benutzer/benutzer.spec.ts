import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Benutzer } from './benutzer';

describe('Benutzer', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Benutzer],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zeigt Benutzer und speichert eine Mandanten-Zuweisung', async () => {
    const fixture = TestBed.createComponent(Benutzer);
    fixture.detectChanges();

    httpMock.expectOne('/api/mandanten').flush([
      { id: 1, name: 'Mustermann GmbH', status: 'AKTIV' },
      { id: 2, name: 'Beispiel Handel GmbH', status: 'AKTIV' },
    ]);
    httpMock.expectOne('/api/benutzer').flush([
      { sub: 'u-leser', benutzername: 'leser', alleMandanten: false, mandanten: ['Mustermann GmbH'] },
    ]);

    await fixture.whenStable();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('leser');

    (fixture.componentInstance as unknown as { speichere(b: unknown): void }).speichere({
      sub: 'u-leser',
      benutzername: 'leser',
      alleMandanten: false,
      mandanten: ['Mustermann GmbH', 'Beispiel Handel GmbH'],
    });
    const put = httpMock.expectOne('/api/benutzer');
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual({
      sub: 'u-leser',
      alleMandanten: false,
      mandanten: ['Mustermann GmbH', 'Beispiel Handel GmbH'],
    });
    put.flush({ sub: 'u-leser', benutzername: 'leser', alleMandanten: false, mandanten: ['Mustermann GmbH', 'Beispiel Handel GmbH'] });
  });
});
