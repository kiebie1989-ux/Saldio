package com.bwa.controlling.stammdaten;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Schlüssel/Wert-Parameter der Anwendung (Berichtsmodus, Zielwerte, Schalter).
 * Entspricht der Parametertabelle im Excel-Blatt 01_Einstellungen.
 */
@Entity
@Table(name = "einstellung")
public class Einstellung {

    @Id
    private String schluessel;

    @Column(name = "wert")
    private String wert;

    private String beschreibung;

    protected Einstellung() {}

    public String getSchluessel() { return schluessel; }
    public String getWert() { return wert; }
    public String getBeschreibung() { return beschreibung; }

    public void setWert(String wert) { this.wert = wert; }
}
