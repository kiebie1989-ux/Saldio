import { Injectable, inject, signal } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { buildAuthConfig, DEFAULT_CONFIG, RuntimeConfig } from './auth.config';

export type Rolle = 'leser' | 'bearbeiter' | 'admin';

/**
 * Kapselt die OIDC-Anbindung an Keycloak: Login/Logout, Authentifizierungsstatus,
 * Benutzername und Rollen (aus dem Access-Token). Einzige Auth-Quelle im Frontend.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly oauth = inject(OAuthService);

  readonly authenticated = signal(false);
  readonly username = signal('');
  readonly roles = signal<string[]>([]);

  /** Beim App-Start: Laufzeit-Konfiguration + Discovery laden, vorhandene Session aufnehmen. */
  async init(): Promise<void> {
    this.oauth.configure(buildAuthConfig(await ladeRuntimeConfig()));
    this.oauth.setupAutomaticSilentRefresh();
    this.oauth.events.subscribe(() => this.aktualisiere());
    try {
      await this.oauth.loadDiscoveryDocumentAndTryLogin();
    } catch (e) {
      console.error('Auth-Initialisierung fehlgeschlagen (läuft Keycloak auf :8081?)', e);
    }
    this.aktualisiere();
  }

  login(): void {
    this.oauth.initCodeFlow();
  }

  logout(): void {
    this.oauth.logOut();
  }

  /** Rollencheck mit Hierarchie: admin > bearbeiter > leser. */
  hasRole(rolle: Rolle): boolean {
    const r = this.roles();
    if (r.includes('admin')) {
      return true;
    }
    if (rolle === 'bearbeiter') {
      return r.includes('bearbeiter');
    }
    if (rolle === 'leser') {
      return r.includes('leser') || r.includes('bearbeiter');
    }
    return false;
  }

  private aktualisiere(): void {
    this.authenticated.set(this.oauth.hasValidAccessToken());
    const claims = this.oauth.getIdentityClaims() as { preferred_username?: string } | null;
    this.username.set(claims?.preferred_username ?? '');
    this.roles.set(this.extrahiereRollen());
  }

  private extrahiereRollen(): string[] {
    const token = this.oauth.getAccessToken();
    if (!token) {
      return [];
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as { realm_access?: { roles?: string[] } };
      return payload.realm_access?.roles ?? [];
    } catch {
      return [];
    }
  }
}

/** Lädt /config.json (relativ zur base href); fällt bei Fehler auf die Dev-Defaults zurück. */
async function ladeRuntimeConfig(): Promise<RuntimeConfig> {
  try {
    const url = new URL('config.json', document.baseURI).toString();
    const resp = await fetch(url, { cache: 'no-cache' });
    if (!resp.ok) {
      return DEFAULT_CONFIG;
    }
    return { ...DEFAULT_CONFIG, ...(await resp.json()) };
  } catch {
    return DEFAULT_CONFIG;
  }
}
