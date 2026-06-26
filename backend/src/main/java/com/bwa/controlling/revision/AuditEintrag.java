package com.bwa.controlling.revision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/** Ein unveränderlicher Eintrag im Audit-Trail (wer/wann/was). */
@Entity
@Table(name = "audit_log")
public class AuditEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime zeitpunkt;

    @Column(name = "benutzer_sub")
    private String benutzerSub;

    private String benutzername;

    @Column(nullable = false)
    private String aktion;

    @Column(nullable = false)
    private String entitaet;

    @Column(name = "entitaet_ref")
    private String entitaetRef;

    private String details;

    protected AuditEintrag() {}

    public AuditEintrag(String benutzerSub, String benutzername, String aktion,
                        String entitaet, String entitaetRef, String details) {
        this.benutzerSub = benutzerSub;
        this.benutzername = benutzername;
        this.aktion = aktion;
        this.entitaet = entitaet;
        this.entitaetRef = entitaetRef;
        this.details = details;
    }

    public Long getId() { return id; }
    public OffsetDateTime getZeitpunkt() { return zeitpunkt; }
    public String getBenutzerSub() { return benutzerSub; }
    public String getBenutzername() { return benutzername; }
    public String getAktion() { return aktion; }
    public String getEntitaet() { return entitaet; }
    public String getEntitaetRef() { return entitaetRef; }
    public String getDetails() { return details; }
}
