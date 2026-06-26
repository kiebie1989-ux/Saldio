# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/).

## [0.1.0] - 2026-06-26 — first public beta

First public open-source release. Beta: functional and tested, not yet proven in long-term
production. Contributions welcome.

### Added
- Brand identity: project named **Saldio**, with an SVG logo and favicon (`frontend/public/logo.svg`,
  `favicon.svg`) and the app title/tab set accordingly.
- Open-source release readiness: MIT `LICENSE`, `THIRD-PARTY-NOTICES.md`, contributing/security/
  conduct docs, issue & PR templates, bilingual README.
- One-command **Quickstart** (`scripts/quickstart.sh`): self-signed TLS, demo logins, demo data.
- Interactive **installer** (`setup.sh`) generating a complete `.env` with strong secrets, plus
  `scripts/check-env.sh` validation.
- Domain-decoupled configuration: a single `.env` (APP_ORIGIN/AUTH_ORIGIN) drives the Keycloak
  realm redirects, OIDC issuer, frontend runtime config and CORS.

### Changed
- Keycloak realm is now rendered from a template at startup; production realm ships **without**
  default users (first admin via bootstrap console).
- Frontend `config.json` is rendered at container start from environment variables.
- Demo/sample data removed from the production migration path; loaded only in tests and the Quickstart.

## Project history

Built in phases prior to the open-source release:
P0–P6 (core: import, GuV/Bilanz engine, KPIs, dashboard, reports, AI, forecast),
authentication (Keycloak/OIDC, roles, tenant isolation), and production hardening
A–F (correctness, GoBD audit features, security, deployment, UX, QA & acceptance).
