package com.sanisidro.restaurante.features.invoices.service;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sanisidro.restaurante.features.orders.model.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceGenerator {

    private final TemplateEngine templateEngine;

    

    public byte[] generateInvoice(Order order) {

        Locale peruLocale = new Locale("es", "PE");

        Context context = new Context(peruLocale);
        context.setVariable("order", order);

        String processedHtml;
        try {
            log.info("Procesando plantilla 'invoice.html' para la orden #{}", order.getId());
            processedHtml = templateEngine.process("invoice.html", context);
        } catch (Exception e) {
            log.error("Error fatal al procesar la plantilla Thymeleaf 'invoice.html'", e);
            throw new RuntimeException("Error procesando plantilla HTML", e);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(processedHtml, null);
            builder.toStream(out);
            builder.run();

            log.info("PDF de comprobante generado exitosamente para la orden #{}", order.getId());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error al convertir HTML a PDF para la orden #{}", order.getId(), e);
            throw new RuntimeException("Error generando el PDF del comprobante", e);
        }
    }
}