# Security Policy / Sicherheitsrichtlinie

## Reporting a vulnerability / Schwachstelle melden

Please report security issues **privately**, not via public issues.
Bitte Sicherheitsprobleme **privat** melden, nicht über öffentliche Issues.

- Use GitHub's "Report a vulnerability" (Security Advisories) for this repository, or
- contact the maintainer privately.

Nutze "Report a vulnerability" (GitHub Security Advisories) oder kontaktiere den Maintainer privat.

Please include affected version/commit, reproduction steps, and impact.
Bitte betroffene Version/Commit, Reproduktionsschritte und Auswirkung angeben.

## Scope notes / Hinweise

- The **Quickstart** ships fixed demo credentials and a self-signed certificate — it is for local
  evaluation only and must never be exposed to the internet.
  Der **Quickstart** nutzt feste Demo-Zugänge und ein self-signed Zertifikat — nur lokal, niemals
  öffentlich exponieren.
- For production use `setup.sh` (generates strong secrets) and a real domain (Let's-Encrypt TLS).
  Für Produktion `setup.sh` (starke Secrets) und eine echte Domain (Let's-Encrypt-TLS) verwenden.
- See [`docs/security-review.md`](docs/security-review.md) for the hardening review and open items.
