# Verfahrensdokumentation — DATEV-BWA-Controlling

Stand: lebendes Dokument. Beschreibt das DV-gestützte Verfahren gemäß GoBD-Anforderungen
(Nachvollziehbarkeit, Unveränderbarkeit, Vollständigkeit, Berechtigungen, Aufbewahrung).

> **Hinweis:** Dieses Dokument beschreibt die *technischen* Kontrollen der Software. Die *formale*
> GoBD-Konformität erfordert zusätzlich organisatorische Maßnahmen (Zugriffsorganisation,
> interne Kontrollen, Datensicherungskonzept) und eine steuerlich/juristische Abnahme durch den
> verantwortlichen Steuerberater. Diese liegen außerhalb der Software.

## 1. Überblick & Zweck

Das System übernimmt DATEV-Buchungsdaten (Saldenlisten/Buchungsstapel) mehrerer Mandanten,
ordnet sie über einen Kontenrahmen den BWA-Gruppen zu und erstellt daraus GuV, Bilanz, Kennzahlen,
Berichte und Auswertungen. Es ist mehrbenutzerfähig mit rollen- und mandantenbasierter Zugriffskontrolle.

## 2. Datenfluss

1. **Import** (`POST /api/import`, Rolle Bearbeiter+): CSV-Saldenliste oder DATEV-EXTF-Buchungsstapel.
   - DATEV-Mandantennummer (EXTF) wird zum Mandantennamen aufgelöst; unbekannte Nummer ⇒
     Zeilenstatus `WARN_MANDANT_UNBEKANNT`.
   - Soll/Haben + Kontenklasse ⇒ vorzeichenrichtiger Betrag je BWA-Gruppe (Storno reduziert).
   - **Doppel-Import-Schutz:** identischer Dateiinhalt (SHA-256) wird abgelehnt.
2. **Verarbeitung:** Aggregation in PostgreSQL (SUMIFS-Äquivalent) ⇒ GuV/Bilanz/Kennzahlen/Forecast.
3. **Auswertung/Anzeige:** Frontend (rollen- und mandantengefiltert).
4. **Festschreibung:** Perioden (Mandant+Monat) werden gesiegelt; danach keine Buchungen mehr.
5. **Export:** prüfbarer GoBD-Export je Mandant/Jahr (Buchungen + Hashkette + Integritätsnachweis).

## 3. Unveränderbarkeit (GoBD §)

- Buchungen sind **append-only**: ein Datenbank-Trigger (`trg_buchung_append_only`) verbietet
  UPDATE und DELETE auf der Tabelle `buchung`.
- Korrekturen erfolgen ausschließlich per **Storno** (`POST /api/import/{id}/storno`): es entsteht ein
  Gegenstapel mit negierten Buchungen; das Original bleibt unverändert und referenziert.
- Festgeschriebene Perioden sind gegen weitere Buchungen **und** Storno gesperrt.

## 4. Festschreibung & Manipulationssicherheit

- Festschreibung einer Periode (`POST /api/festschreibung`, Rolle Admin) berechnet einen
  `periodenHash = SHA-256(vorgaengerHash + kanonische Buchungen der Periode)`.
- Die Hashes bilden je Mandant eine **Kette**; nachträgliche Änderungen werden durch erneute
  Berechnung erkannt (`GET /api/festschreibung/pruefen`).

## 5. Audit-Trail

- Tabelle `audit_log` protokolliert lückenlos mutierende Operationen mit Benutzer (Keycloak-`sub`),
  Zeitpunkt, Aktion, Entität und Details: IMPORT, STORNO, FESTSCHREIBUNG, EINSTELLUNG_AENDERN,
  MANDANTEN_ZUWEISUNG, GOBD_EXPORT.

## 6. Rollen & Berechtigungen

- Authentifizierung via Keycloak (OIDC). Rollen: **Leser** (lesen), **Bearbeiter** (+Import/Storno),
  **Admin** (+Einstellungen, Benutzerverwaltung, Festschreibung, Export).
- **Mandanten-Datentrennung:** jeder Benutzer (am `sub`) sieht nur die ihm zugewiesenen Mandanten
  (secure-by-default: ohne Zuweisung kein Zugriff); Admin sieht alle. Durchgesetzt serverseitig auf
  allen mandantenbezogenen Endpunkten und Listen.

## 7. Aufbewahrung

- Steuerrelevante Daten (Buchungen, Festschreibungen, Audit-Log) werden nicht gelöscht
  (append-only). Aufbewahrungsfrist: 10 Jahre (organisatorisch zu überwachen).

## 8. Datensicherung & Betrieb

- On-Prem-Betrieb (Docker Compose) mit TLS-Reverse-Proxy. PostgreSQL-Backups mit Retention
  (siehe Deployment-/Betriebsdokumentation, Phase D). Migrationen versioniert über Flyway.

## 9. Offene/organisatorische Punkte

- Formaler DSFinV-K-/GDPdU-Export (amtliches Format) als Ausbauthema.
- Organisatorische Verfahrensbeschreibung (Zugriffsorganisation, IKS, Notfallkonzept) durch die Kanzlei.
- Steuerlich/juristische Abnahme der GoBD-Konformität.
