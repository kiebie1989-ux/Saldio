# Security-Review

Fokussierter Review der sicherheitsrelevanten Fläche (Auth, Eingaben, Injection, Secrets, Transport).
Stand: Phase F. Der automatisierte `/security-review` (PR-Diff gegen Remote) war mangels Remote nicht
anwendbar — daher manueller Review entlang der OWASP-Schwerpunkte.

## Geprüft — in Ordnung

- **Authentifizierung/Autorisierung:** OAuth2-Resource-Server validiert Keycloak-JWT (Issuer + JWKS).
  Endpunkt-Autorisierung explizit je Pfad/Methode (`SecurityConfig`); Admin-/Bearbeiter-/Leser-Rollen.
  Mandanten-Datentrennung serverseitig erzwungen (`MandantenZugriffService.pruefe`) auf allen
  mandantenbezogenen Endpunkten und Listen. Tests: `SecurityRulesIT`, `MandantenZugriffIT`, `AuthControllerIT`.
- **SQL-Injection:** ausschließlich JPQL/Spring-Data mit gebundenen Parametern; keine String-konkatenierten
  Queries mit Nutzereingaben.
- **Massenzuweisung/IDs:** Schreib-Endpunkte nehmen explizite DTOs; Mandant/Einstellung/Benutzer per
  ID/Schlüssel; kein Durchreichen ganzer Entities aus dem Request.
- **CSRF:** bewusst deaktiviert — zustandsloses Bearer-Token-Modell (kein Cookie-Session-Login).
- **CORS:** auf konfigurierte Origins beschränkt (`bwa.cors.allowed-origins`); fremde Origin → 403 (Test).
- **Security-Header:** `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, HSTS.
- **Upload:** Größenlimit (20 MB); Doppel-Import-Schutz (Hash).
- **Secrets:** Prod-Profil ohne Default-Secrets (Start bricht ab, wenn Pflicht-Env fehlt); `.env` gitignored.
- **Revisionssicherheit:** Buchungen append-only (DB-Trigger), lückenloser Audit-Trail, Festschreibung.
- **Abhängigkeiten:** `npm audit` (prod) 0 Schwachstellen; Backend-Versionen via Spring-Boot-BOM.

## Behoben

- Ungültiger Import-`typ` ergab HTTP 500 → jetzt **400 Bad Request** (`ImportController`, Test ergänzt).

## Empfohlene weitere Härtung (dokumentiert, vor Produktivgang umzusetzen)

- **Content-Security-Policy (CSP):** im nginx/Reverse-Proxy setzen (`default-src 'self'`,
  `connect-src 'self' <AUTH_DOMAIN>` …). Muss mit der konkreten Auth-Domain getestet werden,
  da eine zu strenge CSP die Keycloak-XHR/Token-Calls blockieren kann.
- **Rate-Limiting / Brute-Force am Edge:** auf Login-/API-Pfaden (Caddy/Reverse-Proxy); Keycloak-seitig
  ist Brute-Force-Schutz bereits aktiv.
- **Keycloak `sslRequired`:** im Prod-Realm auf `external` setzen (TLS terminiert am Reverse-Proxy).
- **Token-Speicherung im SPA:** Standard-Risiko; CSP + kurze Token-Lebensdauer (bereits 5 min) mindern.
- **Penetrationstest** vor Echtbetrieb (organisatorisch).
