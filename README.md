<p align="center">
  <img src="frontend/public/logo.svg" alt="Saldio" width="84" height="84" />
</p>

<h1 align="center">Saldio</h1>

<p align="center">
  <a href="LICENSE"><img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-blue.svg" /></a>
</p>

Self-hosted controlling & reporting tool for German *Betriebswirtschaftliche Auswertung* (BWA)
on a DATEV basis (SKR03/SKR04). Multi-tenant, with profit-&-loss / balance-sheet aggregation,
KPIs with traffic-light thresholds, an executive dashboard, PDF reports, optional self-hosted
AI commentary, and GoBD-grade audit features (append-only bookings, audit trail, period sealing).

Selbstgehostetes Controlling-/Reporting-Werkzeug für die deutsche Betriebswirtschaftliche
Auswertung (BWA) auf DATEV-Basis (SKR03/SKR04). Mehrmandantenfähig, mit GuV-/Bilanz-Aggregation,
Kennzahlen mit Ampellogik, Dashboard, PDF-Berichten, optionaler self-hosted KI-Auswertung und
GoBD-Bausteinen (append-only Buchungen, Audit-Trail, Festschreibung).

> ⚠️ The technical GoBD controls do not by themselves make an installation legally GoBD-compliant —
> organisational measures and professional/legal sign-off are required. See `docs/abnahme-checkliste.md`.
> Die technischen GoBD-Kontrollen allein stellen keine rechtliche GoBD-Konformität her;
> dafür sind organisatorische Maßnahmen und eine fachliche/juristische Abnahme nötig.

<!-- Screenshots: bitte hier Dashboard- und Bericht-Screenshots einfügen / add dashboard & report screenshots here -->

## Features

- **Import**: DATEV (vereinfachtes CSV und EXTF-Buchungsstapel), Doppel-Import-Schutz, Storno statt Löschen.
- **Engine**: GuV, Bilanz, Zwischensummen, Kennzahlen mit Ampel, Dashboard, Kostenstruktur, Kumulierung, Forecast.
- **Reports**: Mandantenbericht, PDF-Export, optionale KI-Auswertung (self-hosted via Ollama; OpenAI-kompatibel optional).
- **Auth**: OIDC via Keycloak, Rollen `admin/bearbeiter/leser`, mandantenscharfe Datentrennung.
- **GoBD**: append-only Buchungen (DB-Trigger), Audit-Trail, Festschreibung mit Hash-Kette, GoBD-Export.

## Quickstart (zum Ausprobieren / try it out)

Requires Docker + Docker Compose. One command, runs locally with a self-signed certificate and demo data:

```bash
git clone https://github.com/kiebie1989-ux/Saldio.git
cd Saldio
./scripts/quickstart.sh
```

Open **https://bwa.127.0.0.1.nip.io:8943** (accept the self-signed cert; also open
`https://auth.127.0.0.1.nip.io:8943` once and accept it). Demo logins: `admin/admin`, `leser/leser`.

> Demo only — do not expose to the internet. `nip.io` resolves to `127.0.0.1`.

## Production deployment (eigene Domain / your own domain)

Requires two DNS records (app + auth) pointing at the host and ports 80/443 open. Caddy obtains
Let's-Encrypt certificates automatically.

```bash
./setup.sh                                 # asks for domains, generates secrets, writes .env
./scripts/check-env.sh                      # validates .env
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Then create your first app admin in the Keycloak admin console (`https://<auth-domain>/admin`,
realm `bwa`, role `admin`). Details: [`docs/betrieb.md`](docs/betrieb.md).

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.4, JPA/Hibernate, Flyway, Spring AI |
| Database | PostgreSQL 16 |
| Frontend | Angular 22, Angular Material, ECharts |
| Auth | Keycloak (OIDC) |
| Reverse proxy / TLS | Caddy (automatic HTTPS) |
| AI (optional) | Ollama (self-hosted) |

## Development

```bash
docker compose up -d postgres keycloak     # dev dependencies
cd backend && mvn spring-boot:run          # http://localhost:8080
cd frontend && npm install && npx ng serve # http://localhost:4200
```

## Tests

```bash
cd backend && mvn verify                    # 17 unit + 65 integration (Testcontainers, needs Docker)
cd frontend && npx ng test --watch=false    # 23 component tests
./scripts/e2e.sh                            # Playwright E2E (real Keycloak login)
```

## Documentation

- [`docs/betrieb.md`](docs/betrieb.md) — operations / Betrieb (deutsch)
- [`docs/abnahme-checkliste.md`](docs/abnahme-checkliste.md) — acceptance checklist / Abnahme
- [`docs/verfahrensdokumentation.md`](docs/verfahrensdokumentation.md) — GoBD process docs
- [`docs/security-review.md`](docs/security-review.md) — security review

## License

[MIT](LICENSE). Third-party components retain their own licenses — see
[`THIRD-PARTY-NOTICES.md`](THIRD-PARTY-NOTICES.md). No GPL/AGPL dependencies.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).
Security issues: [`SECURITY.md`](SECURITY.md).
