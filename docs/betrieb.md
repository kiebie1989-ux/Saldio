# Betriebsdokumentation — On-Prem-Deployment

Produktivbetrieb via Docker Compose (`docker-compose.prod.yml`) mit TLS-Reverse-Proxy (Caddy),
Keycloak (Prod-Modus, eigene DB), App-Postgres, Backend, Frontend und optional self-hosted KI (Ollama).

## Voraussetzungen

- Linux-Host mit Docker + Docker Compose v2.
- Zwei DNS-Einträge auf den Host: `APP_DOMAIN` (App) und `AUTH_DOMAIN` (Keycloak); Ports 80/443 offen
  (Caddy holt automatisch Let's-Encrypt-Zertifikate).

## Erstinstallation

1. `.env` aus `.env.example` erstellen und **alle Secrets** setzen (DB-, Keycloak-DB-, Bootstrap-Admin-
   Passwörter, Domains, `CORS_ALLOWED_ORIGINS=https://APP_DOMAIN`, `KEYCLOAK_ISSUER_URI=https://AUTH_DOMAIN/realms/bwa`,
   `KEYCLOAK_JWK_SET_URI=.../protocol/openid-connect/certs`).
2. `infra/frontend/config.prod.json` anpassen: `issuer` = `https://AUTH_DOMAIN/realms/bwa`, `requireHttps: true`.
3. Start: `docker compose -f docker-compose.prod.yml --env-file .env up -d --build`.
4. Keycloak: der Realm `bwa` wird importiert. **Initial-Admin-Passwörter sofort ändern**, Seed-Benutzer
   (`admin`, `leser`) durch echte Benutzer ersetzen, im Frontend unter „Benutzer & Zugriff" Mandanten zuweisen.
5. KI-Modell ziehen (falls genutzt): `docker exec bwa-ollama ollama pull <modell>` und `OLLAMA_MODEL` setzen.

## Updates

`git pull && docker compose -f docker-compose.prod.yml --env-file .env up -d --build`.
Flyway migriert die DB beim Start automatisch (nicht-destruktiv, versioniert).

## Backup & Restore

- **Backup** (Cron, täglich): `scripts/backup.sh` (pg_dump beider DBs, gzip, Retention `RETENTION_DAYS`).
- **Restore:** Stack stoppen; `gunzip -c bwa-<ts>.sql.gz | docker exec -i bwa-app-db psql -U $DB_USER -d bwa`
  (analog Keycloak-DB); Stack starten.
- Volumes `app_db_data`, `kc_db_data` zusätzlich in die Host-Datensicherung aufnehmen.

## Monitoring & Logs

- Healthchecks je Container; App-Health: `https://APP_DOMAIN/actuator/health`.
- Logs: `docker compose -f docker-compose.prod.yml logs -f <service>`; für zentrale Sammlung an einen
  Log-Collector anbinden.

## Sicherheit (Betrieb)

- TLS endet an Caddy; intern HTTP im Docker-Netz. Keycloak mit `KC_PROXY_HEADERS=xforwarded`.
- Keine Default-Secrets im Prod-Profil (Start bricht ab, wenn Pflicht-Env fehlt).
- Rollen/Mandanten-Trennung serverseitig erzwungen; Buchungen append-only; Audit-Trail aktiv
  (siehe `docs/verfahrensdokumentation.md`).

## Offene Betriebsthemen

- Zentrales Monitoring/Alerting (Prometheus/Grafana) und Log-Aggregation projektspezifisch ergänzen.
- Regelmäßige Restore-Tests der Backups.
