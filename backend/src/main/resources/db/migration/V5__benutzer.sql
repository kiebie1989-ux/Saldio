-- B: Benutzer (am Keycloak-sub) und ihre Mandanten-Berechtigung.

CREATE TABLE benutzer (
    sub             TEXT PRIMARY KEY,
    benutzername    TEXT,
    alle_mandanten  BOOLEAN     NOT NULL DEFAULT FALSE,
    erstellt_am     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE benutzer_mandant (
    sub      TEXT NOT NULL REFERENCES benutzer (sub) ON DELETE CASCADE,
    mandant  TEXT NOT NULL,
    PRIMARY KEY (sub, mandant)
);
