#!/usr/bin/env bash
# Startet den kompletten BWA-Stack lokal auf freien Ports (kollidiert nicht mit anderen Apps)
# und importiert die Beispiel-Saldenliste, damit Auswertungen befüllt sind.
#   Keycloak 8081 · Postgres 55432 · Backend 18080 · Frontend 14200
# Dienste laufen detached weiter; Stoppen: scripts/stop-local.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
LOG=/tmp/bwa-local
mkdir -p "$LOG"

echo "==> Keycloak (8081) + Postgres (55432)"
docker compose up -d keycloak >/dev/null
docker rm -f bwa-pg-local >/dev/null 2>&1 || true
docker run -d --name bwa-pg-local -e POSTGRES_DB=bwa -e POSTGRES_USER=bwa -e POSTGRES_PASSWORD=bwa \
  -p 55432:5432 postgres:16-alpine >/dev/null
for i in $(seq 1 30); do docker exec bwa-pg-local pg_isready -U bwa -d bwa >/dev/null 2>&1 && break; sleep 1; done
for i in $(seq 1 90); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/realms/bwa/.well-known/openid-configuration)" = "200" ] && break; sleep 1; done

echo "==> Backend (18080)"
setsid bash -c "cd '$ROOT/backend' && SERVER_PORT=18080 DB_URL=jdbc:postgresql://localhost:55432/bwa \
  KEYCLOAK_JWK_SET_URI=http://localhost:8081/realms/bwa/protocol/openid-connect/certs \
  KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/bwa \
  java -jar target/controlling-backend-0.1.0-SNAPSHOT.jar" >"$LOG/backend.log" 2>&1 < /dev/null &
echo $! > "$LOG/backend.pid"
for i in $(seq 1 60); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:18080/api/health)" = "200" ] && break; sleep 1; done

echo "==> Beispieldaten importieren"
ATOK=$(curl -s -d "grant_type=password&client_id=bwa-app&username=admin&password=admin" \
  http://localhost:8081/realms/bwa/protocol/openid-connect/token | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
curl -s -H "Authorization: Bearer $ATOK" -F "typ=csv" \
  -F "datei=@backend/src/test/resources/samples/saldenliste_2025_voll.csv" \
  http://localhost:18080/api/import >/dev/null && echo "   Saldenliste importiert"

echo "==> Frontend (14200)"
setsid bash -c "cd '$ROOT/frontend' && npx ng serve --port 14200 --proxy-config proxy.e2e.conf.json" \
  >"$LOG/frontend.log" 2>&1 < /dev/null &
echo $! > "$LOG/frontend.pid"
for i in $(seq 1 180); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:14200)" = "200" ] && break; sleep 1; done

echo
echo "FERTIG. Im Browser öffnen:  http://localhost:14200"
echo "Login (Keycloak):  admin / admin   (Admin, sieht alle Mandanten)"
echo "             oder:  leser / leser   (zunächst ohne Mandanten -> über 'Benutzer & Zugriff' zuweisen)"
