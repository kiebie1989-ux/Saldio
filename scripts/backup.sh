#!/usr/bin/env bash
# Sichert die App- und Keycloak-Datenbanken (für Cron geeignet). Liest .env.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
[ -f "$ROOT/.env" ] && set -a && . "$ROOT/.env" && set +a

DIR="${BACKUP_DIR:-/var/backups/bwa}"
RET="${RETENTION_DAYS:-30}"
TS="$(date +%Y%m%d-%H%M%S)"
mkdir -p "$DIR"

echo "Sichere App-DB -> $DIR/bwa-$TS.sql.gz"
docker exec bwa-app-db pg_dump -U "${DB_USER}" bwa | gzip > "$DIR/bwa-$TS.sql.gz"

echo "Sichere Keycloak-DB -> $DIR/keycloak-$TS.sql.gz"
docker exec bwa-keycloak-db pg_dump -U "${KC_DB_USERNAME}" keycloak | gzip > "$DIR/keycloak-$TS.sql.gz"

echo "Lösche Backups älter als ${RET} Tage"
find "$DIR" -name '*.sql.gz' -mtime +"${RET}" -delete
echo "Backup fertig."
