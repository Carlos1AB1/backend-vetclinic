package com.vetclinic.patterns.factory.report;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Factory Method Pattern
 * Interfaz para generadores de reportes
 * RF015 - Reportes Operativos
 */
public interface ReportGenerator {
    
    /**
     * Generar reporte según los parámetros especificados
     * 
     * @param parameters Parámetros del reporte (fechas, filtros, etc.)
     * @return Bytes del reporte generado
     */
    ByteArrayOutputStream generateReport(Map<String, Object> parameters);
    
    /**
     * Obtener el tipo de reporte
     */
    String getReportType();
    
    /**
     * Obtener la extensión del archivo
     */
    String getFileExtension();
    
    /**
     * Obtener el MIME type
     */
    String getMimeType();
}



