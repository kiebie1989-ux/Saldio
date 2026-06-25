import { AuthConfig } from 'angular-oauth2-oidc';

/**
 * OIDC-Konfiguration für Keycloak (Realm "bwa", Public-Client "bwa-app").
 * Authorization Code Flow + PKCE. Issuer für die lokale Entwicklung; in Produktion
 * über Environment/Build-Konfiguration setzen.
 */
export const authConfig: AuthConfig = {
  issuer: 'http://localhost:8081/realms/bwa',
  redirectUri: window.location.origin + '/',
  postLogoutRedirectUri: window.location.origin + '/',
  clientId: 'bwa-app',
  responseType: 'code',
  scope: 'openid profile email',
  requireHttps: false, // lokale Entwicklung über http
  showDebugInformation: false,
};
