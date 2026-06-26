package com.bwa.controlling.stammdaten;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mandant (Firma) mit Steuerungsflags: in welcher Berichtssicht er erscheint.
 * Entspricht der Mandantensteuerung im Excel-Blatt 01_Einstellungen.
 */
@Entity
@Table(name = "mandant")
public class Mandant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean imEinzelbericht;

    @Column(nullable = false)
    private boolean inKumulierung;

    @Column(nullable = false)
    private boolean imFinalbericht;

    private String typ;
    private String bemerkung;

    @Column(name = "datev_mandantennr")
    private String datevMandantennr;

    @Column(name = "datev_beraternr")
    private String datevBeraternr;

    protected Mandant() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public boolean isImEinzelbericht() { return imEinzelbericht; }
    public boolean isInKumulierung() { return inKumulierung; }
    public boolean isImFinalbericht() { return imFinalbericht; }
    public String getTyp() { return typ; }
    public String getBemerkung() { return bemerkung; }
    public String getDatevMandantennr() { return datevMandantennr; }
    public String getDatevBeraternr() { return datevBeraternr; }
}
