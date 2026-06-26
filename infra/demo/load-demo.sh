#!/bin/sh
# Quickstart-Demo-Loader: wartet, bis das App-Schema (Flyway) steht, und spielt die Demo-Daten ein.
set -e
echo "[demo-seed] warte auf App-Schema (Tabelle mandant)..."
until psql -h app-db -U "$DB_USER" -d bwa -tAc "SELECT to_regclass('public.mandant')" | grep -q mandant; do
  sleep 2
done
echo "[demo-seed] spiele Demo-Daten ein..."
psql -h app-db -U "$DB_USER" -d bwa -f /demo/demo-data.sql
echo "[demo-seed] fertig."
