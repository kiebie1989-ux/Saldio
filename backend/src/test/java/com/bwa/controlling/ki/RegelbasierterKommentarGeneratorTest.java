package com.bwa.controlling.ki;

import com.bwa.controlling.engine.Kennzahl.Ampel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Golden-Tests des deterministischen Generators (Ampel -> Bewertung/Maßnahme/Text). */
class RegelbasierterKommentarGeneratorTest {

    private final RegelbasierterKommentarGenerator generator = new RegelbasierterKommentarGenerator();

    @Test
    void erzeugtBereichsanalysenMitKorrekterAmpel() {
        Mandantenbericht b = generator.erzeuge(KommentarFixtures.mustermann());

        assertThat(b.quelle()).isEqualTo("regelbasiert");
        assertThat(b.bereiche()).hasSize(6);

        var ebit = bereich(b, "EBIT");
        assertThat(ebit.ampel()).isEqualTo(Ampel.GRUEN);
        assertThat(ebit.bewertung()).isEqualTo("im grünen Bereich");
        assertThat(ebit.massnahme()).isEqualTo("Niveau halten");

        var sonstiges = bereich(b, "Sonstiges");
        assertThat(sonstiges.ampel()).isEqualTo(Ampel.ROT);
        assertThat(sonstiges.bewertung()).isEqualTo("kritisch");
        assertThat(sonstiges.massnahme()).isEqualTo("Ursachen analysieren");
    }

    @Test
    void managementkommentarHatSechsAbschnitteUndMeldetKritischeKosten() {
        Mandantenbericht b = generator.erzeuge(KommentarFixtures.mustermann());

        assertThat(b.managementkommentar()).hasSize(6);
        assertThat(b.managementkommentar().get(0).titel()).isEqualTo("1. Geschäftsentwicklung");

        String kosten = b.managementkommentar().get(2).text();
        assertThat(kosten).contains("KRITISCH"); // Sonstigenquote ist rot
    }

    private static Mandantenbericht.BereichsAnalyse bereich(Mandantenbericht b, String name) {
        return b.bereiche().stream().filter(x -> x.bereich().equals(name)).findFirst().orElseThrow();
    }
}
