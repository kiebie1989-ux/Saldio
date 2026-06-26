package com.bwa.controlling.revision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Festschreibung (Siegelung) einer Periode (Mandant + Monat). Der {@code periodenHash} ist über
 * alle Buchungen der Periode und den {@code vorgaengerHash} gebildet -> manipulationssichere Kette.
 */
@Entity
@Table(name = "festschreibung")
public class Festschreibung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mandant;

    @Column(nullable = false)
    private int jahr;

    @Column(nullable = false)
    private String monat;

    @CreationTimestamp
    @Column(name = "festgeschrieben_am", nullable = false, updatable = false)
    private OffsetDateTime festgeschriebenAm;

    @Column(name = "festgeschrieben_von")
    private String festgeschriebenVon;

    @Column(name = "anzahl_buchungen", nullable = false)
    private int anzahlBuchungen;

    @Column(name = "vorgaenger_hash")
    private String vorgaengerHash;

    @Column(name = "perioden_hash", nullable = false)
    private String periodenHash;

    protected Festschreibung() {}

    public Festschreibung(String mandant, int jahr, String monat, String festgeschriebenVon,
                          int anzahlBuchungen, String vorgaengerHash, String periodenHash) {
        this.mandant = mandant;
        this.jahr = jahr;
        this.monat = monat;
        this.festgeschriebenVon = festgeschriebenVon;
        this.anzahlBuchungen = anzahlBuchungen;
        this.vorgaengerHash = vorgaengerHash;
        this.periodenHash = periodenHash;
    }

    public Long getId() { return id; }
    public String getMandant() { return mandant; }
    public int getJahr() { return jahr; }
    public String getMonat() { return monat; }
    public OffsetDateTime getFestgeschriebenAm() { return festgeschriebenAm; }
    public String getFestgeschriebenVon() { return festgeschriebenVon; }
    public int getAnzahlBuchungen() { return anzahlBuchungen; }
    public String getVorgaengerHash() { return vorgaengerHash; }
    public String getPeriodenHash() { return periodenHash; }
}
