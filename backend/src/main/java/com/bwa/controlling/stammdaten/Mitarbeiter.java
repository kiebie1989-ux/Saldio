package com.bwa.controlling.stammdaten;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Mitarbeiter mit Lohn- und Stundenbasis (Excel-Blatt 03_Mitarbeiter).
 * Abgeleitete Werte (€/Std, AG-Anteil, Gesamtkosten) werden nicht gespeichert,
 * sondern im Service/DTO berechnet.
 */
@Entity
@Table(name = "mitarbeiter")
public class Mitarbeiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String personalnummer;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mandant;

    private String kostenstelle;
    private String team;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monatslohn;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal stundenProMonat;

    protected Mitarbeiter() {}

    public Long getId() { return id; }
    public String getPersonalnummer() { return personalnummer; }
    public String getName() { return name; }
    public String getMandant() { return mandant; }
    public String getKostenstelle() { return kostenstelle; }
    public String getTeam() { return team; }
    public BigDecimal getMonatslohn() { return monatslohn; }
    public BigDecimal getStundenProMonat() { return stundenProMonat; }
}
