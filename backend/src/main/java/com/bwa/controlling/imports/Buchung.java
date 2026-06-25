package com.bwa.controlling.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Normalisierte Buchungszeile aus dem Import (Excel-Blätter 01_/02_Import). */
@Entity
@Table(name = "buchung")
public class Buchung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_batch_id")
    private Long importBatchId;

    @Column(nullable = false)
    private String quelle;

    @Column(nullable = false)
    private String monat;

    @Column(nullable = false)
    private String mandant;

    @Column(nullable = false)
    private String konto;

    private String bezeichnung;
    private String bwaGruppe;
    private String guvBilanzPosition;
    private String sollHaben;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal betrag;

    private String kostenstelle;

    @Column(nullable = false)
    private String status = "OK";

    protected Buchung() {}

    public Long getId() { return id; }
    public Long getImportBatchId() { return importBatchId; }
    public String getQuelle() { return quelle; }
    public String getMonat() { return monat; }
    public String getMandant() { return mandant; }
    public String getKonto() { return konto; }
    public String getBezeichnung() { return bezeichnung; }
    public String getBwaGruppe() { return bwaGruppe; }
    public String getGuvBilanzPosition() { return guvBilanzPosition; }
    public String getSollHaben() { return sollHaben; }
    public BigDecimal getBetrag() { return betrag; }
    public String getKostenstelle() { return kostenstelle; }
    public String getStatus() { return status; }

    public void setImportBatchId(Long v) { this.importBatchId = v; }
    public void setQuelle(String v) { this.quelle = v; }
    public void setMonat(String v) { this.monat = v; }
    public void setMandant(String v) { this.mandant = v; }
    public void setKonto(String v) { this.konto = v; }
    public void setBezeichnung(String v) { this.bezeichnung = v; }
    public void setBwaGruppe(String v) { this.bwaGruppe = v; }
    public void setGuvBilanzPosition(String v) { this.guvBilanzPosition = v; }
    public void setSollHaben(String v) { this.sollHaben = v; }
    public void setBetrag(BigDecimal v) { this.betrag = v; }
    public void setKostenstelle(String v) { this.kostenstelle = v; }
    public void setStatus(String v) { this.status = v; }
}
