#!/usr/bin/env bash
# Stoppt den lokal gestarteten BWA-Stack (scripts/start-local.sh).
LOG=/tmp/bwa-local
[ -f "$LOG/frontend.pid" ] && kill "$(cat "$LOG/frontend.pid")" 2>/dev/null && echo "Frontend gestoppt" || true
[ -f "$LOG/backend.pid" ] && kill "$(cat "$LOG/backend.pid")" 2>/dev/null && echo "Backend gestoppt" || true
# ng serve startet einen Kindprozess -> sicherheitshalber per Port beenden
pkill -f "ng serve --port 14200" 2>/dev/null || true
pkill -f "controlling-backend-0.1.0-SNAPSHOT.jar" 2>/dev/null || true
docker rm -f bwa-pg-local >/dev/null 2>&1 || true
cd "$(dirname "$0")/.." && docker compose down >/dev/null 2>&1 || true
echo "Stack gestoppt."
