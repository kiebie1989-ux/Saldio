package com.bwa.controlling.revision;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Prüfbarer GoBD-Export (Admin). */
@RestController
@RequestMapping("/api/gobd-export")
public class GobdExportController {

    private final GobdExportService service;

    public GobdExportController(GobdExportService service) {
        this.service = service;
    }

    @GetMapping
    public GobdExportService.GobdExport export(@RequestParam String mandant, @RequestParam int jahr) {
        return service.export(mandant, jahr);
    }
}
