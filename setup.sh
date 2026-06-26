#!/usr/bin/env bash
# Interaktiver Installer für den Produktivbetrieb: fragt die Domains ab, erzeugt starke Secrets
# und schreibt eine vollständige .env. Danach: docker compose -f docker-compose.prod.yml --env-file .env up -d --build
#
# Voraussetzung für echtes TLS: APP_DOMAIN und AUTH_DOMAIN zeigen per DNS auf diesen Host,
# Ports 80/443 sind erreichbar (Caddy holt automatisch Let's-Encrypt-Zertifikate).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$ROOT/.env"

if [ -f "$ENV_FILE" ]; then
  printf '.env existiert bereits. Überschreiben? [y/N] '
  read -r ans
  case "$ans" in y|Y|yes|j|J) ;; *) echo "Abgebrochen."; exit 0;; esac
fi

# Starkes Secret: Base64 ohne Sonderzeichen (sicher in .env und Shell).
gen_secret() { openssl rand -base64 24 | tr -dc 'A-Za-z0-9' | cut -c1-32; }

ask() { # ask VAR "Prompt" ["default"]
  local __var="$1" __prompt="$2" __def="${3:-}" __in
  if [ -n "$__def" ]; then printf '%s [%s]: ' "$__prompt" "$__def"; else printf '%s: ' "$__prompt"; fi
  read -r __in
  printf -v "$__var" '%s' "${__in:-$__def}"
}

echo "== BWA Controlling – Produktiv-Setup =="
ask APP_DOMAIN  "App-Domain (z.B. bwa.example.com)"
ask AUTH_DOMAIN "Auth-Domain/Keycloak (z.B. auth.example.com)"
ask KC_ADMIN    "Keycloak-Admin-Benutzername" "admin"

if [ -z "${APP_DOMAIN:-}" ] || [ -z "${AUTH_DOMAIN:-}" ]; then
  echo "FEHLER: App- und Auth-Domain sind Pflicht." >&2; exit 1
fi

DB_PASSWORD="$(gen_secret)"
KC_BOOTSTRAP_ADMIN_PASSWORD="$(gen_secret)"
KC_DB_PASSWORD="$(gen_secret)"

umask 077
cat > "$ENV_FILE" <<EOF
# Erzeugt von setup.sh am $(date -Iseconds). Enthält Geheimnisse - nicht committen, nicht teilen.

# --- Domains (Caddy-TLS, automatisches Let's-Encrypt) + Origins (eine Quelle der Wahrheit
#     für Realm-Redirects, OIDC-Issuer, Frontend-Config und CORS) ---
APP_DOMAIN=$APP_DOMAIN
AUTH_DOMAIN=$AUTH_DOMAIN
APP_ORIGIN=https://$APP_DOMAIN
AUTH_ORIGIN=https://$AUTH_DOMAIN

# --- Datenbank (App) ---
DB_USER=bwa
DB_PASSWORD=$DB_PASSWORD

# --- Keycloak Bootstrap-Admin (erste Anmeldung an der Admin-Konsole) + eigene DB ---
KC_BOOTSTRAP_ADMIN_USERNAME=$KC_ADMIN
KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_BOOTSTRAP_ADMIN_PASSWORD
KC_DB_USERNAME=keycloak
KC_DB_PASSWORD=$KC_DB_PASSWORD

# --- KI (self-hosted Ollama) ---
BWA_KI_PROVIDER=ollama
OLLAMA_MODEL=llama3.1:8b

# --- Backup ---
BACKUP_DIR=/var/backups/bwa
RETENTION_DAYS=30
EOF
chmod 600 "$ENV_FILE"

echo
echo "OK: .env geschrieben (Secrets generiert)."
echo "Keycloak-Bootstrap-Admin: $KC_ADMIN / $KC_BOOTSTRAP_ADMIN_PASSWORD"
echo "   -> nach dem ersten Start unter $AUTH_DOMAIN/admin den ersten App-Admin im Realm 'bwa' anlegen"
echo "      (Realm-Rolle 'admin' zuweisen)."
echo
echo "Naechste Schritte:"
echo "  ./scripts/check-env.sh"
echo "  docker compose -f docker-compose.prod.yml --env-file .env up -d --build"
