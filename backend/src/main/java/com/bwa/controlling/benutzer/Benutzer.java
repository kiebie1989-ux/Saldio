package com.bwa.controlling.benutzer;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Anwendungsbenutzer, identifiziert über den Keycloak-{@code sub}-Claim.
 * Hält die Mandanten, die der Benutzer sehen darf (sofern nicht {@code alleMandanten}).
 */
@Entity
@Table(name = "benutzer")
public class Benutzer {

    @Id
    private String sub;

    private String benutzername;

    @Column(name = "alle_mandanten", nullable = false)
    private boolean alleMandanten;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "benutzer_mandant", joinColumns = @JoinColumn(name = "sub"))
    @Column(name = "mandant")
    private Set<String> mandanten = new HashSet<>();

    protected Benutzer() {}

    public Benutzer(String sub, String benutzername) {
        this.sub = sub;
        this.benutzername = benutzername;
    }

    public String getSub() { return sub; }
    public String getBenutzername() { return benutzername; }
    public boolean isAlleMandanten() { return alleMandanten; }
    public Set<String> getMandanten() { return mandanten; }

    public void setBenutzername(String benutzername) { this.benutzername = benutzername; }
    public void setAlleMandanten(boolean alleMandanten) { this.alleMandanten = alleMandanten; }
    public void setMandanten(Set<String> mandanten) { this.mandanten = mandanten; }
}
