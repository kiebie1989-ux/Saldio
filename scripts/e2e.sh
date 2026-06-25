#!/usr/bin/env bash
# End-to-End-Lauf (Playwright) über den echten Keycloak-Login gegen den vollen Stack.
#
# Nutzt bewusst freie Ports, damit es nicht mit anderen lokal laufenden Apps kollidiert:
#   Keycloak 8081 (compose) · Postgres 55432 (standalone) · Backend 18080 · Frontend 14200
#
# Voraussetzung: Backend-Jar gebaut (cd backend && mvn -DskipTests package), Docker läuft,
#   Playwright-Browser installiert (cd frontend && npx playwright install chromium).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

cleanup() {
  kill "${NG:-0}" "${BE:-0}" 2>/dev/null || true
  docker compose down -v >/dev/null 2>&1 || true
  docker rm -f bwa-pg-e2e >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "==> Keycloak + Postgres starten"
docker compose down -v >/dev/null 2>&1 || true
docker rm -f bwa-pg-e2e >/dev/null 2>&1 || true
docker compose up -d keycloak >/dev/null
docker run -d --name bwa-pg-e2e -e POSTGRES_DB=bwa -e POSTGRES_USER=bwa -e POSTGRES_PASSWORD=bwa \
  -p 55432:5432 postgres:16-alpine >/dev/null
for i in $(seq 1 30); do docker exec bwa-pg-e2e pg_isready -U bwa -d bwa >/dev/null 2>&1 && break; sleep 1; done
for i in $(seq 1 90); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/realms/bwa/.well-known/openid-configuration)" = "200" ] && break; sleep 1; done

echo "==> Backend :18080"
( cd backend && SERVER_PORT=18080 DB_URL=jdbc:postgresql://localhost:55432/bwa \
    KEYCLOAK_JWK_SET_URI=http://localhost:8081/realms/bwa/protocol/openid-connect/certs \
    KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/bwa \
    java -jar target/controlling-backend-0.1.0-SNAPSHOT.jar > /tmp/e2e-be.log 2>&1 ) &
BE=$!
for i in $(seq 1 60); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:18080/api/health)" = "200" ] && break; sleep 1; done

echo "==> Frontend :14200 (Proxy -> 18080)"
( cd frontend && npx ng serve --port 14200 --proxy-config proxy.e2e.conf.json > /tmp/e2e-ng.log 2>&1 ) &
NG=$!
for i in $(seq 1 180); do [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:14200)" = "200" ] && break; sleep 1; done

echo "==> Playwright E2E"
( cd frontend && E2E_BASE_URL=http://localhost:14200 npx playwright test )
