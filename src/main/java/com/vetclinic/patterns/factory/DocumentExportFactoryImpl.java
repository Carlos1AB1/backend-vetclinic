package com.vetclinic.patterns.factory;

import com.vetclinic.patterns.factory.export.DocumentExporter;
import com.vetclinic.patterns.factory.export.ExcelDocumentExporter;
import com.vetclinic.patterns.factory.export.PdfDocumentExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern
 * Implementación de la fábrica de exportadores de documentos
 * RF014 - Generación de Recetas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentExportFactoryImpl implements DocumentExportFactory {

    private final PdfDocumentExporter pdfExporter;
    private final ExcelDocumentExporter excelExporter;

    @Override
    public DocumentExporter create(String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("El formato no puede ser nulo o vacío");
        }

        String normalizedFormat = format.toUpperCase().trim();

        return switch (normalizedFormat) {
            case "PDF" -> {
                log.debug("Creando exportador PDF");
                yield pdfExporter;
            }
            case "EXCEL", "XLSX" -> {
                log.debug("Creando exportador Excel");
                yield excelExporter;
            }
            default -> {
                log.warn("Formato no soportado: {}. Usando PDF por defecto", format);
                yield pdfExporter;
            }
        };
    }

    @Override
    public DocumentExporter createDefault() {
        return pdfExporter;
    }
}



