-- Phase B1: lückenloser Audit-Trail für mutierende Operationen (GoBD).

CREATE TABLE audit_log (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zeitpunkt     TIMESTAMPTZ NOT NULL DEFAULT now(),
    benutzer_sub  TEXT,
    benutzername  TEXT,
    aktion        VARCHAR(60) NOT NULL,
    entitaet      VARCHAR(60) NOT NULL,
    entitaet_ref  TEXT,
    details       TEXT
);
CREATE INDEX idx_audit_log_zeitpunkt ON audit_log (zeitpunkt);
CREATE INDEX idx_audit_log_entitaet ON audit_log (entitaet, entitaet_ref);

-- Wer hat den Import angelegt (Nachvollziehbarkeit am Stapel).
ALTER TABLE import_batch ADD COLUMN erstellt_von TEXT;
