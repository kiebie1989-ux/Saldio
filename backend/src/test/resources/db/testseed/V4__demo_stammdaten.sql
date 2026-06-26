-- Seed: Mandanten, Einstellungen, Mitarbeiter aus Excel (Initialdaten, editierbar).

INSERT INTO mandant (name, status, im_einzelbericht, in_kumulierung, im_finalbericht, typ, bemerkung) VALUES
  ('Mustermann GmbH', 'AKTIV', TRUE, TRUE, TRUE, 'GmbH', 'Hauptmandant'),
  ('Beispiel Handel GmbH', 'AKTIV', TRUE, TRUE, TRUE, 'GmbH', 'Handelsbereich'),
  ('Alpha Services UG', 'AKTIV', TRUE, TRUE, TRUE, 'UG', 'Dienstleistung'),
  ('Beta Technik GmbH', 'AKTIV', FALSE, TRUE, FALSE, 'GmbH', 'Nur intern'),
  ('Gamma Holding GmbH', 'INAKTIV', FALSE, FALSE, FALSE, 'GmbH', 'Inaktiv / in Gründung');

INSERT INTO einstellung (schluessel, wert, beschreibung) VALUES
  ('Datenquelle BWA', 'DATEV_BWA.csv', 'Pfad/Dateiname der importierten BWA-CSV'),
  ('Datenquelle Mitarbeiter', 'Mitarbeiter.csv', 'Pfad/Dateiname der Personal-CSV'),
  ('Mandantenmodus', 'Kumuliert', 'Welche Sicht wird im Report angezeigt'),
  ('Aktiver Mandant', 'Mustermann GmbH', 'Wird im Einzelmodus als Fokus-Mandant angezeigt'),
  ('Berichtsmonat', 'Sep 2025', 'Monatsfokus für die Reportseite'),
  ('Diagrammstil', 'Executive', 'Layoutvariante für Charts und Reports'),
  ('KI-Kommentar', 'Aktiv', 'Automatische KI-Textauswertung ein/aus'),
  ('Kostenstellenanalyse', 'Aktiv', 'Drill-down auf Teams und Kostenstellen'),
  ('Mandanten kumulieren', 'Ja', 'Zusammenfassung aller aktiven Firmen'),
  ('Vorjahresvergleich', 'Aktiv', 'VJ-Spalten in Dashboard und Bericht einblenden'),
  ('Ziel-EBIT-Marge %', '18', 'Schwellwert für Ampel-Logik EBIT'),
  ('Ziel-Liquidität %', '120', 'Mindestziel Liquidität 1. Grades');

INSERT INTO mitarbeiter (personalnummer, name, mandant, kostenstelle, team, monatslohn, stunden_pro_monat) VALUES
  ('E001', 'Anna Schmidt', 'Mustermann GmbH', 'IT', 'Dev', 5200, 160),
  ('E002', 'Ben Müller', 'Mustermann GmbH', 'IT', 'Support', 4300, 160),
  ('E003', 'Cem Yilmaz', 'Mustermann GmbH', 'Sales', 'Beratung', 6100, 168),
  ('E004', 'Dana Weber', 'Mustermann GmbH', 'Finance', 'Buchhaltung', 4800, 160),
  ('E101', 'Erik Bauer', 'Beispiel Handel GmbH', 'Vertrieb', 'Außendienst', 4900, 172),
  ('E102', 'Fatma Kaya', 'Alpha Services UG', 'Service', 'Consulting', 5500, 160);
