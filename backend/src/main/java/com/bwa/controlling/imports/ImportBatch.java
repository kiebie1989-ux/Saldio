package com.bwa.controlling.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/** Ein Importvorgang: Nachvollziehbarkeit und Status je hochgeladener Datei. */
@Entity
@Table(name = "import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dateiname;

    @Column(nullable = false)
    private String quelle;

    @CreationTimestamp
    @Column(name = "importiert_am", nullable = false, updatable = false)
    private OffsetDateTime importiertAm;

    @Column(nullable = false)
    private int zeilenGesamt;

    @Column(nullable = false)
    private int zeilenOk;

    @Column(nullable = false)
    private int zeilenWarnung;

    @Column(nullable = false)
    private String status = "OK";

    protected ImportBatch() {}

    public ImportBatch(String dateiname, String quelle) {
        this.dateiname = dateiname;
        this.quelle = quelle;
    }

    public Long getId() { return id; }
    public String getDateiname() { return dateiname; }
    public String getQuelle() { return quelle; }
    public OffsetDateTime getImportiertAm() { return importiertAm; }
    public int getZeilenGesamt() { return zeilenGesamt; }
    public int getZeilenOk() { return zeilenOk; }
    public int getZeilenWarnung() { return zeilenWarnung; }
    public String getStatus() { return status; }

    public void setZeilenGesamt(int v) { this.zeilenGesamt = v; }
    public void setZeilenOk(int v) { this.zeilenOk = v; }
    public void setZeilenWarnung(int v) { this.zeilenWarnung = v; }
    public void setStatus(String v) { this.status = v; }
}
