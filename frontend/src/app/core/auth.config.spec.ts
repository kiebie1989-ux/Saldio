import { buildAuthConfig, DEFAULT_CONFIG } from './auth.config';

describe('buildAuthConfig', () => {
  it('übernimmt die Laufzeit-Konfiguration', () => {
    const cfg = buildAuthConfig({
      issuer: 'https://kc.example.com/realms/bwa',
      clientId: 'bwa-app',
      requireHttps: true,
    });
    expect(cfg.issuer).toBe('https://kc.example.com/realms/bwa');
    expect(cfg.clientId).toBe('bwa-app');
    expect(cfg.requireHttps).toBe(true);
    expect(cfg.responseType).toBe('code');
    expect(cfg.redirectUri).toContain(window.location.origin);
  });

  it('hat sinnvolle Dev-Defaults', () => {
    expect(DEFAULT_CONFIG.issuer).toContain('localhost:8081');
    expect(DEFAULT_CONFIG.requireHttps).toBe(false);
  });
});
