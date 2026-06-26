-- Phase A: DATEV-Mandanten-/Beraternummer am Mandanten (für EXTF-Zuordnung)
-- und Datei-Hash am Import-Stapel (Doppel-Import-Schutz).

ALTER TABLE mandant ADD COLUMN datev_mandantennr VARCHAR(20);
ALTER TABLE mandant ADD COLUMN datev_beraternr   VARCHAR(20);
CREATE INDEX idx_mandant_datev_mandantennr ON mandant (datev_mandantennr);

-- Beispiel-Zuordnung: die DATEV-Mandantennummer des EXTF-Beispiels gehört zu Mustermann GmbH.
UPDATE mandant SET datev_mandantennr = '73841', datev_beraternr = '1001' WHERE name = 'Mustermann GmbH';
UPDATE mandant SET datev_mandantennr = '73842', datev_beraternr = '1001' WHERE name = 'Beispiel Handel GmbH';

ALTER TABLE import_batch ADD COLUMN datei_hash VARCHAR(64);
CREATE INDEX idx_import_batch_datei_hash ON import_batch (datei_hash);

-- Status-Codes wie 'WARN_MANDANT_UNBEKANNT' (22 Zeichen) sprengen VARCHAR(20).
ALTER TABLE buchung ALTER COLUMN status TYPE VARCHAR(40);
