import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { App } from './app';
import { routes } from './app.routes';
import { AuthService } from './core/auth.service';

/** Stub für den AuthService: angemeldeter Admin (sieht alle Navigationseinträge). */
const authStub: Partial<AuthService> = {
  authenticated: (() => true) as AuthService['authenticated'],
  username: (() => 'tester') as AuthService['username'],
  roles: (() => ['admin']) as AuthService['roles'],
  hasRole: () => true,
  login: () => {},
  logout: () => {},
};

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter(routes),
        provideNoopAnimations(),
        { provide: AuthService, useValue: authStub },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    expect(TestBed.createComponent(App).componentInstance).toBeTruthy();
  });

  it('should render the app title and the logged-in user', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-title')?.textContent).toContain('DATEV-BWA Controlling');
    expect(compiled.textContent).toContain('tester');
  });

  it('should render all navigation items for an admin', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const links = (fixture.nativeElement as HTMLElement).querySelectorAll('mat-nav-list a');
    expect(links.length).toBe(13);
  });
});
