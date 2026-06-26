# Abnahme-Checkliste — DATEV-BWA Controlling

Diese Checkliste führt durch die Abnahme vor dem Produktivgang. Sie deckt **technische** Punkte
(im Code/Test belegbar) und **organisatorische** Punkte (durch Steuerberater/Betreiber zu erfüllen).
Status: Stand Phase F. Technische Punkte sind durch automatisierte Tests bzw. den lokalen Stack belegt.

Legende: ✅ erfüllt & getestet · ⚙️ technisch vorbereitet, vor Echtbetrieb zu konfigurieren ·
☐ organisatorisch, außerhalb des Codes.

## 1. Fachliche Korrektheit (Phase A)

- ✅ GuV-/Bilanz-Aggregation reproduziert die Excel-Golden-Values (`GuvEngineIT`, `BilanzEngineIT`).
- ✅ Kennzahlen/Ampel entsprechen den Excel-Formeln (`KennzahlenEngineIT`).
- ✅ Soll/Haben → vorzeichenrichtiger Betrag je Kontenklasse, inkl. Storno/negativ (`BetragsLogikTest`).
- ✅ EXTF-Mandantennummer wird auf den Mandantennamen aufgelöst; unbekannte Nummer ⇒
  `WARN_MANDANT_UNBEKANNT` statt stiller Fehlzuordnung (`ImportPhaseAIT`).
- ✅ Doppel-Import identischer Dateien wird per Inhalts-Hash abgelehnt (`ImportPhaseAIT`).
- ✅ EXTF-Parser gehärtet: namensbasiertes Spalten-Mapping, Encoding/BOM, Pflichtfeld-Validierung
  (`DatevExtfParserTest`).

## 2. GoBD-Revisionssicherheit (Phase B)

- ✅ Buchungen append-only — Update/Delete durch DB-Trigger verhindert (`FestschreibungIT#buchungIstAppendOnly`).
- ✅ Korrektur nur per Storno-Gegenstapel, Original bleibt erhalten (`ImportPhaseAIT`, `FestschreibungIT`).
- ✅ Lückenloser Audit-Trail (wer/wann/Aktion/Entität) für alle mutierenden Operationen (`AuditIT`).
- ✅ Festschreibung je Periode sperrt Folgebuchungen; Hash-Kette erkennt Manipulation (`FestschreibungIT`).
- ✅ GoBD-Export (Buchungen + Stammdaten + Audit) verfügbar und prüfbar (`GobdExportIT`).
- ⚙️ `docs/verfahrensdokumentation.md` ist gepflegt — vor Abnahme mit dem realen Betriebsprozess abgleichen.
- ☐ Juristische/steuerliche Abnahme der GoBD-Konformität (organisatorisch, nicht im Code).
- ☐ Aufbewahrungsfrist (10 Jahre) im Backup-/Retention-Prozess organisatorisch verankern.

## 3. Sicherheit & Zugriff (Phase C)

- ✅ OAuth2-Resource-Server validiert Keycloak-JWT (Issuer + JWKS).
- ✅ Rollen `admin/bearbeiter/leser` je Endpunkt erzwungen (`SecurityRulesIT`).
- ✅ Mandanten-Datentrennung serverseitig, secure-by-default (`MandantenZugriffIT`).
- ✅ Security-Header (HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy), CORS auf
  konfigurierte Origins beschränkt (`SecurityHardeningIT`).
- ✅ Ungültige Eingaben ergeben 4xx statt 5xx (`ImportControllerIT`); Upload-Größenlimit aktiv.
- ✅ `npm audit` (prod) ohne Schwachstellen; manueller Security-Review dokumentiert (`docs/security-review.md`).
- ⚙️ CSP am Reverse-Proxy setzen (gegen die konkrete Auth-Domain testen, s. Security-Review).
- ⚙️ Keycloak `sslRequired=external`, Brute-Force-Schutz, kurze Token-Lebensdauern im Prod-Realm.
- ☐ Penetrationstest vor Echtbetrieb (organisatorisch).

## 4. Deployment & Betrieb (Phase D)

- ✅ Container-Images: Backend (multi-stage, non-root), Frontend (nginx + SPA/API-Proxy).
- ✅ `docker-compose.prod.yml`: Caddy (TLS) · Keycloak(prod) + DB · App-DB · Backend · Frontend · Ollama;
  Healthchecks, Restart-Policy, Volumes.
- ✅ Backup-Skript (`scripts/backup.sh`) + dokumentierter Restore (`docs/betrieb.md`).
- ⚙️ `.env` aus `.env.example` mit echten Secrets befüllen (keine Default-Passwörter im Prod-Profil).
- ⚙️ Domain/Zertifikat im `Caddyfile` und `infra/frontend/config.prod.json` (Issuer/API) eintragen.
- ☐ Backup→Restore mindestens einmal real durchspielen; Monitoring/Alarmierung anbinden.

## 5. Funktionsvollständigkeit & UX (Phase E)

- ✅ Jahresauswahl auf allen 9 Berichtsseiten; verfügbare Jahre aus den Daten (`GET /api/jahre`).
- ✅ Globale Fehlerbehandlung (401→Login, 403/5xx→Hinweis) über HTTP-Interceptor.
- ✅ Stammdaten-Pflege im UI: Mandant anlegen/ändern inkl. DATEV-Mandantennummer (admin-gated).
- ✅ Import-UX: Historie + Storno je Stapel.
- ✅ Lokalisierung `de-DE` (Zahlen/Währung/Datum deutsch).

## 6. Qualitätssicherung (Phase F)

- ✅ Backend: 17 Unit + 65 Integration (Testcontainers) grün — `cd backend && mvn verify`.
- ✅ Frontend: 23 Komponententests grün — `cd frontend && npx ng test --watch=false`.
- ✅ E2E (Playwright): Admin-Login über echten Keycloak + Navigation zu geschützten Berichtsseiten —
  `./scripts/e2e.sh`.
- ✅ Performance: 7.200 Buchungen importiert, GuV-Aggregation < 5 s (real ~12 ms) — `PerformanceIT`.
- ✅ Security-Review dokumentiert (`docs/security-review.md`).

## Abnahme-Sign-off

| Bereich | Verantwortlich | Datum | Unterschrift |
|---|---|---|---|
| Fachliche Korrektheit | Steuerberater | | |
| GoBD-Konformität (juristisch) | Steuerberater/Berater | | |
| Sicherheit/Betrieb | Betreiber/IT | | |
| Abnahme gesamt | Auftraggeber | | |
