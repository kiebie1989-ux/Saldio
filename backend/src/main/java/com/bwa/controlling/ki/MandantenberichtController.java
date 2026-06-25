package com.bwa.controlling.ki;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST-Zugriff auf den Mandantenbericht als JSON und als PDF. */
@RestController
@RequestMapping("/api")
public class MandantenberichtController {

    private final MandantenberichtService berichtService;
    private final PdfService pdfService;
    private final com.bwa.controlling.benutzer.MandantenZugriffService zugriff;

    public MandantenberichtController(MandantenberichtService berichtService, PdfService pdfService,
                                      com.bwa.controlling.benutzer.MandantenZugriffService zugriff) {
        this.berichtService = berichtService;
        this.pdfService = pdfService;
        this.zugriff = zugriff;
    }

    @GetMapping("/mandantenbericht")
    public Mandantenbericht bericht(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return berichtService.erzeuge(mandant, jahr);
    }

    @GetMapping("/mandantenbericht/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        byte[] pdf = pdfService.alsPdf(berichtService.erzeuge(mandant, jahr));
        String dateiname = "Mandantenbericht_%s_%d.pdf".formatted(mandant.replaceAll("\\s+", "_"), jahr);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(dateiname).build().toString())
                .body(pdf);
    }
}
