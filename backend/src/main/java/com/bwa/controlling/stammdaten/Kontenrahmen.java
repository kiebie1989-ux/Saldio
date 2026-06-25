package com.bwa.controlling.stammdaten;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Ein Konto des Kontenrahmens: Zuordnung SKR03/SKR04 -> BWA-Gruppe und GuV/Bilanz-Position.
 * Bildet die VLOOKUP-Logik des Excel-Blatts 00_Kontenrahmen ab.
 */
@Entity
@Table(name = "kontenrahmen")
public class Kontenrahmen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skr03;
    private String skr04;

    @Column(nullable = false)
    private String bezeichnung;

    private String bwaGruppe;
    private String guvBilanzPosition;
    private String kontenklasse;
    private String vorzeichen;

    @Column(nullable = false)
    private boolean aktiv;

    protected Kontenrahmen() {}

    public Long getId() { return id; }
    public String getSkr03() { return skr03; }
    public String getSkr04() { return skr04; }
    public String getBezeichnung() { return bezeichnung; }
    public String getBwaGruppe() { return bwaGruppe; }
    public String getGuvBilanzPosition() { return guvBilanzPosition; }
    public String getKontenklasse() { return kontenklasse; }
    public String getVorzeichen() { return vorzeichen; }
    public boolean isAktiv() { return aktiv; }
}
