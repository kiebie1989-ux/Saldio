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
  BWA Controlling - Quickstart laeuft.

  URL:    https://bwa.127.0.0.1.nip.io:8943
          (Self-signed-Zertifikat -> im Browser einmal bestaetigen.
           Auch https://auth.127.0.0.1.nip.io:8943 einmal oeffnen und bestaetigen.)

  Logins: admin / admin   (Vollzugriff)
          leser / leser   (nur lesen; Mandanten unter "Benutzer & Zugriff" zuweisen)

  Demo-Daten (Beispiel-Mandanten) werden im Hintergrund geladen.

  Hinweis: NUR zum Ausprobieren. Fuer den echten Betrieb: ./setup.sh + docker-compose.prod.yml.
  Stoppen:  docker compose -f docker-compose.prod.yml -f docker-compose.quickstart.yml --env-file .env.quickstart down
  (Daten loeschen: zusaetzlich -v)
============================================================
EOF
