-- Baseline-Migration des DATEV-BWA-Controlling-Schemas.
-- Fachliche Tabellen (Kontenrahmen, Mandant, Buchung, ...) folgen in P2.
-- Diese Tabelle dient als Bootstrap-/Sanity-Marker und beweist, dass Flyway läuft.

CREATE TABLE schema_bootstrap (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    component   TEXT        NOT NULL,
    applied_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_bootstrap (component) VALUES ('baseline');
