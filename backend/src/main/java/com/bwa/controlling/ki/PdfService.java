package com.bwa.controlling.ki;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

/**
 * Rendert den Mandantenbericht über ein Thymeleaf-Template (wohlgeformtes XHTML) und
 * Flying Saucer (OpenPDF) zu einem PDF-Dokument.
 */
@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] alsPdf(Mandantenbericht bericht) {
        Context context = new Context();
        context.setVariable("bericht", bericht);
        String xhtml = templateEngine.process("mandantenbericht", context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF-Erzeugung fehlgeschlagen", e);
        }
    }
}
