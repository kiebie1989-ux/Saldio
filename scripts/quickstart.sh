#!/usr/bin/env bash
# Startet die sofort lauffähige Demo (kein eigener Server, keine Domain nötig).
# Voraussetzung: Docker + Docker Compose.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Baue und starte den Quickstart-Stack (das erste Mal dauert es einige Minuten)..."
docker compose -f docker-compose.prod.yml -f docker-compose.quickstart.yml --env-file .env.quickstart up -d --build

cat <<'EOF'

============================================================
  Saldio - Quickstart laeuft.

  Self-signed-TLS: BEIDE Zertifikate einmal akzeptieren - in DIESER Reihenfolge.
  Die vollstaendige URL inkl. https:// UND :8943 verwenden.

  1) Auth-Zertifikat akzeptieren - diese URL oeffnen und die Warnung bestaetigen
     ("Erweitert" -> "fortfahren"), bis das JSON erscheint:
        https://auth.127.0.0.1.nip.io:8943/realms/bwa/.well-known/openid-configuration
     OHNE diesen Schritt: "Server nicht verfuegbar" und Login schlaegt fehl.

  2) App oeffnen (Zertifikatswarnung ebenfalls bestaetigen):
        https://bwa.127.0.0.1.nip.io:8943

  Logins: admin / admin  (Vollzugriff)   ·   leser / leser  (nur lesen)

  Demo-Daten (Beispiel-Mandanten) werden im Hintergrund geladen.

  Hinweis: NUR zum Ausprobieren (self-signed). Fuer den echten Betrieb mit eigener
  Domain (gueltiges Let's-Encrypt-Zertifikat, kein Akzeptieren noetig):
     ./setup.sh  +  docker compose -f docker-compose.prod.yml --env-file .env up -d --build
  Stoppen:  docker compose -f docker-compose.prod.yml -f docker-compose.quickstart.yml --env-file .env.quickstart down
  (Daten loeschen: zusaetzlich -v)
============================================================
EOF
