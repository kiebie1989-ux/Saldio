# DATEV-BWA Controlling

Moderne Ablösung der Excel-Mappe `DATEV_BWA_Reporting_Master.xlsx` — ein Controlling-Werkzeug
für Steuerberater zur monatlichen Betriebswirtschaftlichen Auswertung (BWA) mehrerer Mandanten
auf DATEV-Basis (SKR03/SKR04).

## Stack

| Schicht | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3.4, JPA/Hibernate, Flyway |
| Datenbank | PostgreSQL 16 |
| Frontend | Angular 22, Angular Material, ngx-echarts (ECharts) |
| KI | Spring AI (Multi-Provider: Ollama lokal, OpenAI-kompatibel/OpenRouter/DeepSeek/Anthropic) |
| Tests | JUnit 5, Testcontainers, AssertJ (Backend); Vitest/jsdom (Frontend) |

## Projektstruktur

```
backend/            Spring-Boot-Anwendung (package-by-feature: stammdaten, imports, engine, report, ki, api)
frontend/           Angular-Workspace (Feature-Routen je Excel-Blatt)
docker-compose.yml  Lokales PostgreSQL für die Entwicklung
poc-datev-import/   PoC-Gate: DATEV-CSV/EXTF-Import -> Postgres (bewiesen)
poc-llm/            PoC-Gate: Spring-AI Multi-Provider-LLM (gegen Ollama bewiesen)
```

## Lokal starten

```bash
# 1. Datenbank
docker compose up -d postgres

# 2. Backend (http://localhost:8080)
cd backend && mvn spring-boot:run

# 3. Frontend (http://localhost:4200, /api -> :8080 via Proxy)
cd frontend && npm install && npx ng serve
```

## Tests

```bash
# Backend (Unit + Integration via Testcontainers; benötigt laufendes Docker)
cd backend && mvn verify

# Frontend
cd frontend && npx ng test --watch=false
```

> Hinweis: In Umgebungen mit sehr neuem Docker-Daemon benötigt Testcontainers
> `-Dapi.version=1.43` — bereits fest in der Surefire-/Failsafe-Konfiguration hinterlegt.

## Roadmap

- **P0 PoC-Gate** ✅ DATEV-Import + Multi-Provider-LLM bewiesen
- **P1 Skelett** ✅ Backend + Frontend + CI + Health-E2E
- **P2 Stammdaten & Import** ✅ Kontenrahmen, Mandanten, Einstellungen, Mitarbeiter; CSV + EXTF
- **P3 Engine GuV + Bilanz** ✅ monatliche Aggregation + Zwischensummen, gegen Excel-Golden-Values verifiziert
- **P4 Kennzahlen + Ampel + Dashboard** ✅ 11 BWA-Ratios mit Ampel, Dashboard-Aggregat; Frontend-Dashboard + Kennzahlen-Seite mit echten Daten
- **P5 Mandantenbericht + KI-Auswertung + PDF** ✅ Multi-Provider-KI (Spring AI, Ollama live) + regelbasiertes Fallback, PDF-Export (Flying Saucer); Frontend Mandantenbericht + KI-Auswertung
- **P6 Kumuliert/Final + Planung/Forecast + Kostenstruktur** ✅ mandantenübergreifende Aggregation, Szenario-Forecast, Kostenarten/-stellen; drei Frontend-Seiten

## Authentifizierung / Mehrbenutzer ✅

OIDC via **Keycloak** (Apache-2.0; Zitadel wegen AGPL-3.0 verworfen). Backend = OAuth2-Resource-Server, Rollen `admin/bearbeiter/leser`.
- ✅ PoC-Gate: echter Keycloak-Token-Flow (Login → JWKS-Validierung → Rollen-Mapping → `hasRole`).
- ✅ A — alle `/api/**` rollenbasiert abgesichert (Leser liest, Bearbeiter importiert, Admin ändert Einstellungen/Benutzer).
- ✅ C — Frontend-Login (`angular-oauth2-oidc`): Login-Redirect, Route-Guards, automatischer Bearer-Interceptor, Login/Logout im Header, rollenabhängige Navigation.
- ✅ B — Mandanten-Datentrennung je Benutzer (am `sub`-Claim): secure-by-default, Admin weist Mandanten über die Seite „Benutzer & Zugriff" zu; alle mandantenbezogenen Endpunkte und Listen werden gefiltert/geprüft.

Keycloak lokal: `docker compose up -d keycloak` (Konsole :8081, Seed-User `admin/admin` & `leser/leser`).
**Hinweis:** Mit aktivem Frontend-Login muss Keycloak für die lokale Entwicklung laufen — sonst leitet die App beim Start zum (nicht erreichbaren) Login.

## Tests

```bash
cd backend && mvn verify          # 17 Unit + 65 Integration (Testcontainers)
cd frontend && npx ng test --watch=false   # 23 Komponententests (Vitest)
./scripts/e2e.sh                  # Playwright-E2E: echter Keycloak-Login + Navigation (freie Ports 8081/55432/18080/14200)
```

## Produktionsreife (Phasen A–F)

Aufbauend auf P0–P6 + Auth wurde das System für den On-Prem-Echtbetrieb gehärtet:

- **A — Fachliche Korrektheit** ✅ EXTF-Mandantennummer→Name, vorzeichenrichtige Soll/Haben-Logik
  (`BetragsLogik`), Doppel-Import-Schutz (Hash), gehärteter EXTF-Parser.
- **B — GoBD-Revisionssicherheit** ✅ Buchungen append-only (DB-Trigger), lückenloser Audit-Trail,
  Festschreibung mit SHA-256-Hash-Kette, Storno statt Delete, GoBD-Export, Verfahrensdokumentation.
- **C — Sicherheit & Härtung** ✅ Prod-Keycloak (Brute-Force, kurze Tokens), Security-Header, CORS,
  Secrets externalisiert (kein Default-Passwort im Prod-Profil), Frontend-Runtime-Config.
- **D — Deployment** ✅ `docker-compose.prod.yml` (Caddy/TLS · Keycloak+DB · App-DB · Backend ·
  Frontend · Ollama), Container-Images (non-root), Backup-Skript, Betriebsdoku.
- **E — UX/Vollständigkeit** ✅ Jahresauswahl (datengetrieben), globale Fehlerbehandlung,
  Stammdaten-CRUD im UI, Import-Storno, Lokalisierung `de-DE`.
- **F — QS & Abnahme** ✅ Performancetest (7.200 Buchungen, GuV-Aggregation ~12 ms), Security-Review,
  erweiterte E2E, Abnahme-Checkliste.

Dokumente: [`docs/abnahme-checkliste.md`](docs/abnahme-checkliste.md) ·
[`docs/verfahrensdokumentation.md`](docs/verfahrensdokumentation.md) ·
[`docs/security-review.md`](docs/security-review.md) · [`docs/betrieb.md`](docs/betrieb.md).

> **GoBD-Hinweis:** Der Code liefert die *technischen* Kontrollen. Die *formale* GoBD-Konformität
> erfordert zusätzlich organisatorische Maßnahmen und eine juristische/steuerliche Abnahme
> (siehe Abnahme-Checkliste, Abschnitt 2).

## Status & Restpunkte

P0–P6, die komplette Authentifizierung (Keycloak/OIDC, Rollen, Mandanten-Datentrennung), alle 13
Feature-Routen und die Produktionshärtung A–F sind umgesetzt und getestet — inklusive Playwright-E2E
über den echten Login. Vor dem Echtbetrieb sind die ⚙️-Punkte der Abnahme-Checkliste zu
konfigurieren (Domain/Zertifikat, echte Secrets, CSP, Backup-Restore-Probe).

- **DATEV-EXTF:** Parser gehärtet (Format-/Kategorie-Validierung, UTF-8-BOM, namensbasiertes, reihenfolge-unabhängiges Spalten-Mapping) und breit getestet. Restkaveat: die kanonische Spaltenliste je Formatversion ist proprietäre, zugangsbeschränkte DATEV-Doku — durch das namensbasierte Mapping aber unkritisch; bei Bedarf gegen die offizielle Schnittstellenbeschreibung abgleichen.
- **Cloud-LLM:** OpenAI-kompatibler Pfad (OpenRouter/OpenAI/DeepSeek) gegen einen Mock-Server verifiziert (Request/Response, Auth-Header). Ein echter Cloud-Call braucht nur einen Key:
  ```bash
  BWA_KI_PROVIDER=openrouter OPENAI_API_KEY=sk-or-... OPENAI_MODEL=... \
    java -jar backend/target/controlling-backend-0.1.0-SNAPSHOT.jar
  ```
  Für belastbare KI-Texte ein stärkeres Modell als das lokale 1B wählen.
