import { AuthConfig } from 'angular-oauth2-oidc';

/** Zur Laufzeit aus /config.json geladene Umgebungs-Konfiguration ("build once, deploy anywhere"). */
export interface RuntimeConfig {
  issuer: string;
  clientId: string;
  requireHttps: boolean;
}

/** Fallback für lokale Entwicklung, falls config.json nicht erreichbar ist. */
export const DEFAULT_CONFIG: RuntimeConfig = {
  issuer: 'http://localhost:8081/realms/bwa',
  clientId: 'bwa-app',
  requireHttps: false,
};

/** Baut die OIDC-Konfiguration (Authorization Code + PKCE) aus der Laufzeit-Konfiguration. */
export function buildAuthConfig(cfg: RuntimeConfig): AuthConfig {
  return {
    issuer: cfg.issuer,
    redirectUri: window.location.origin + '/',
    postLogoutRedirectUri: window.location.origin + '/',
    clientId: cfg.clientId,
    responseType: 'code',
    scope: 'openid profile email',
    requireHttps: cfg.requireHttps,
    showDebugInformation: false,
  };
}
