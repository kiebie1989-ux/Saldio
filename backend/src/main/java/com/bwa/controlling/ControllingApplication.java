package com.bwa.controlling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt des DATEV-BWA-Controlling-Backends.
 *
 * Fachliche Pakete (package-by-feature) — schrittweise gefüllt entlang der Roadmap:
 *   stammdaten · imports · engine · report · ki · api
 */
@SpringBootApplication
public class ControllingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllingApplication.class, args);
    }
}
