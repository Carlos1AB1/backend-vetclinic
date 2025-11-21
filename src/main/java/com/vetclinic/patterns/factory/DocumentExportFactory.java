package com.vetclinic.patterns.factory;

import com.vetclinic.patterns.factory.export.DocumentExporter;

/**
 * Factory Method Pattern
 * Factory para crear diferentes tipos de exportadores de documentos
 * RF014 - Generación de Recetas
 */
public interface DocumentExportFactory {
    
    /**
     * Crear un exportador de documentos según el formato especificado
     * 
     * @param format Formato: PDF, EXCEL, WORD
     * @return Exportador de documentos correspondiente
     */
    DocumentExporter create(String format);
    
    /**
     * Crear exportador por defecto (PDF)
     */
    DocumentExporter createDefault();
}



