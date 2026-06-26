#!/usr/bin/env bash
# Validiert die .env vor dem Produktivstart: alle Pflichtvariablen gesetzt, keine Platzhalter.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="${1:-$ROOT/.env}"

[ -f "$ENV_FILE" ] || { echo "FEHLER: $ENV_FILE fehlt. Erst ./setup.sh ausführen."; exit 1; }
set -a; . "$ENV_FILE"; set +a

REQUIRED=(APP_DOMAIN AUTH_DOMAIN APP_ORIGIN AUTH_ORIGIN \
          DB_USER DB_PASSWORD \
          KC_BOOTSTRAP_ADMIN_USERNAME KC_BOOTSTRAP_ADMIN_PASSWORD KC_DB_USERNAME KC_DB_PASSWORD)

fehler=0
for v in "${REQUIRED[@]}"; do
  val="${!v:-}"
  if [ -z "$val" ]; then
    echo "FEHLER: $v ist nicht gesetzt"; fehler=1
  elif printf '%s' "$val" | grep -qiE 'CHANGE_ME|example\.com'; then
    echo "FEHLER: $v enthält noch einen Platzhalter: $val"; fehler=1
  fi
done

# Origins sollten mit https:// beginnen (echtes TLS).
for v in APP_ORIGIN AUTH_ORIGIN; do
  case "${!v:-}" in https://*) ;; *) echo "WARNUNG: $v beginnt nicht mit https:// (${!v:-})";; esac
done

if [ "$fehler" -eq 0 ]; then
  echo "OK: $ENV_FILE ist vollständig und ohne Platzhalter."
else
  echo "-> .env unvollständig. Mit ./setup.sh erzeugen oder Werte ergänzen."; exit 1
fi
