-- P2: Stammdaten- und Import-Tabellen.

-- Kontenrahmen: SKR03/SKR04 -> BWA-Gruppe, GuV/Bilanz-Position, Vorzeichen, Aktiv.
-- Referenzdaten (entspricht Excel-Blatt 00_Kontenrahmen).
CREATE TABLE kontenrahmen (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    skr03                VARCHAR(10),
    skr04                VARCHAR(10),
    bezeichnung          VARCHAR(200) NOT NULL,
    bwa_gruppe           VARCHAR(60),
    guv_bilanz_position  VARCHAR(120),
    kontenklasse         VARCHAR(40),
    vorzeichen           VARCHAR(3),
    aktiv                BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_kontenrahmen_skr03 ON kontenrahmen (skr03);
CREATE INDEX idx_kontenrahmen_skr04 ON kontenrahmen (skr04);

-- Mandant: Steuerung welche Firma in welcher Sicht erscheint (Excel-Blatt 01_Einstellungen).
CREATE TABLE mandant (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              VARCHAR(200) NOT NULL UNIQUE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'AKTIV',
    im_einzelbericht  BOOLEAN      NOT NULL DEFAULT FALSE,
    in_kumulierung    BOOLEAN      NOT NULL DEFAULT FALSE,
    im_finalbericht   BOOLEAN      NOT NULL DEFAULT FALSE,
    typ               VARCHAR(20),
    bemerkung         VARCHAR(255)
);

-- Einstellungen: Schlüssel/Wert-Parameter (Berichtsmodus, Zielwerte, Schalter).
CREATE TABLE einstellung (
    schluessel    VARCHAR(60) PRIMARY KEY,
    wert          VARCHAR(255),
    beschreibung  VARCHAR(255)
);

-- Mitarbeiter: Personalkostenmatrix (Excel-Blatt 03_Mitarbeiter).
CREATE TABLE mitarbeiter (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    personalnummer     VARCHAR(20) NOT NULL UNIQUE,
    name               VARCHAR(200) NOT NULL,
    mandant            VARCHAR(200) NOT NULL,
    kostenstelle       VARCHAR(60),
    team               VARCHAR(60),
    monatslohn         NUMERIC(12,2) NOT NULL,
    stunden_pro_monat  NUMERIC(8,2)  NOT NULL
);

-- Import-Stapel: Nachvollziehbarkeit und Status je Importvorgang.
CREATE TABLE import_batch (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dateiname       VARCHAR(255),
    quelle          VARCHAR(20)  NOT NULL,
    importiert_am   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    zeilen_gesamt   INTEGER      NOT NULL DEFAULT 0,
    zeilen_ok       INTEGER      NOT NULL DEFAULT 0,
    zeilen_warnung  INTEGER      NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OK'
);

-- Buchung: normalisierte Rohdaten aus dem Import (Excel-Blätter 01_/02_Import).
CREATE TABLE buchung (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    import_batch_id      BIGINT REFERENCES import_batch (id),
    quelle               VARCHAR(20)  NOT NULL,
    monat                VARCHAR(7)   NOT NULL,
    mandant              VARCHAR(200) NOT NULL,
    konto                VARCHAR(10)  NOT NULL,
    bezeichnung          VARCHAR(255),
    bwa_gruppe           VARCHAR(60),
    guv_bilanz_position  VARCHAR(120),
    soll_haben           VARCHAR(2),
    betrag               NUMERIC(15,2) NOT NULL,
    kostenstelle         VARCHAR(60),
    status               VARCHAR(20)  NOT NULL DEFAULT 'OK'
);
CREATE INDEX idx_buchung_mandant_monat ON buchung (mandant, monat);
CREATE INDEX idx_buchung_konto ON buchung (konto);
