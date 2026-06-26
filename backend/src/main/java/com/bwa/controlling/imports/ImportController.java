package com.bwa.controlling.imports;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/** Upload-Endpoint für DATEV-Importe (vereinfachtes CSV und EXTF) sowie Import-Historie. */
@RestController
@RequestMapping("/api")
public class ImportController {

    private final ImportService importService;
    private final ImportBatchRepository batchRepository;

    public ImportController(ImportService importService, ImportBatchRepository batchRepository) {
        this.importService = importService;
        this.batchRepository = batchRepository;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportErgebnis importieren(@RequestParam("typ") String typ,
                                      @RequestParam("datei") MultipartFile datei) {
        ImportService.Quelle quelle = ImportService.Quelle.valueOf(typ.toUpperCase(Locale.ROOT));
        try {
            ImportBatch batch = importService.importieren(quelle, datei.getOriginalFilename(), datei.getInputStream());
            return ImportErgebnis.von(batch);
        } catch (IOException e) {
            throw new UncheckedIOException("Datei konnte nicht gelesen werden", e);
        }
    }

    @GetMapping("/import")
    public List<ImportErgebnis> historie() {
        return batchRepository.findAllByOrderByImportiertAmDesc().stream()
                .map(ImportErgebnis::von)
                .toList();
    }

    @PostMapping("/import/{id}/storno")
    public ImportErgebnis stornieren(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ImportErgebnis.von(importService.storniere(id));
    }

    public record ImportErgebnis(Long id, String dateiname, String quelle, OffsetDateTime importiertAm,
                                 int zeilenGesamt, int zeilenOk, int zeilenWarnung, String status) {
        static ImportErgebnis von(ImportBatch b) {
            return new ImportErgebnis(b.getId(), b.getDateiname(), b.getQuelle(), b.getImportiertAm(),
                    b.getZeilenGesamt(), b.getZeilenOk(), b.getZeilenWarnung(), b.getStatus());
        }
    }
}
