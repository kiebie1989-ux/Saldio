-- Phase B2: Festschreibung (Periodensiegelung mit Hash-Kette), Storno-Referenz,
-- und DB-seitige Unveränderbarkeit der Buchungen (GoBD).

CREATE TABLE festschreibung (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mandant             TEXT        NOT NULL,
    jahr                INTEGER     NOT NULL,
    monat               VARCHAR(7)  NOT NULL,
    festgeschrieben_am  TIMESTAMPTZ NOT NULL DEFAULT now(),
    festgeschrieben_von TEXT,
    anzahl_buchungen    INTEGER     NOT NULL,
    vorgaenger_hash     TEXT,
    perioden_hash       TEXT        NOT NULL,
    UNIQUE (mandant, monat)
);

-- Storno-Stapel referenziert den ursprünglichen Stapel (Original bleibt unverändert).
ALTER TABLE import_batch ADD COLUMN storniert_batch_id BIGINT;

-- Buchungen sind append-only: UPDATE/DELETE auf DB-Ebene unterbinden.
CREATE OR REPLACE FUNCTION buchung_append_only() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'buchung ist append-only (GoBD): % nicht erlaubt', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_buchung_append_only
    BEFORE UPDATE OR DELETE ON buchung
    FOR EACH ROW EXECUTE FUNCTION buchung_append_only();
