package com.bwa.controlling.stammdaten;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Prüft Seed-Daten und das Kontenmapping (VLOOKUP-Äquivalent) gegen echtes Postgres. */
@SpringBootTest
class StammdatenIT extends AbstractPostgresIT {

    @Autowired KontenrahmenRepository kontenrahmen;
    @Autowired MandantRepository mandanten;
    @Autowired MitarbeiterRepository mitarbeiter;
    @Autowired EinstellungRepository einstellungen;
    @Autowired KontenmappingService mapping;

    @Test
    void seedDatenSindGeladen() {
        assertThat(kontenrahmen.count()).isEqualTo(84);
        assertThat(mandanten.count()).isEqualTo(5);
        assertThat(mitarbeiter.count()).isEqualTo(6);
        assertThat(einstellungen.count()).isEqualTo(12);
    }

    @Test
    void mandantenflagsAusExcelKorrektGeseedet() {
        Mandant gamma = mandanten.findAllByOrderByNameAsc().stream()
                .filter(m -> m.getName().equals("Gamma Holding GmbH")).findFirst().orElseThrow();
        assertThat(gamma.getStatus()).isEqualTo("INAKTIV");
        assertThat(gamma.isInKumulierung()).isFalse();
    }

    @Test
    void kontenmappingLoestKontenAuf() {
        assertThat(mapping.resolve("8000")).hasValueSatisfying(m -> {
            assertThat(m.bwaGruppe()).isEqualTo("Umsatz");
            assertThat(m.guvBilanzPosition()).isEqualTo("GuV: Umsatzerlöse");
        });
        // SKR04-Schlüssel funktioniert ebenfalls
        assertThat(mapping.resolve("6000")).hasValueSatisfying(m ->
                assertThat(m.guvBilanzPosition()).isEqualTo("GuV: Personalaufwand"));
        assertThat(mapping.resolve("99999")).isEmpty();
    }

    @Test
    void einstellungZielEbitMargeVorhanden() {
        assertThat(einstellungen.findById("Ziel-EBIT-Marge %"))
                .hasValueSatisfying(e -> assertThat(e.getWert()).isEqualTo("18"));
    }
}
