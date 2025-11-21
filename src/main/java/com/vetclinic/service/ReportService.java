package com.vetclinic.service;

import com.vetclinic.patterns.factory.ReportFactory;
import com.vetclinic.patterns.factory.report.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para generar reportes operativos
 * RF015 - Reportes Operativos
 * Usa Factory Method Pattern para crear diferentes tipos de reportes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportFactory reportFactory;

    /**
     * Generar reporte según el tipo especificado
     * 
     * @param reportType Tipo de reporte: APPOINTMENTS, PATIENTS, SERVICES
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @return Bytes del reporte generado
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateReport(String reportType, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generando reporte tipo: {}", reportType);

        // Usar Factory Method Pattern para crear el generador correcto
        ReportGenerator generator = reportFactory.create(reportType);

        // Preparar parámetros
        Map<String, Object> parameters = new HashMap<>();
        if (startDate != null) {
            parameters.put("startDate", startDate);
        } else {
            parameters.put("startDate", LocalDateTime.now().minusMonths(1));
        }
        
        if (endDate != null) {
            parameters.put("endDate", endDate);
        } else {
            parameters.put("endDate", LocalDateTime.now());
        }

        // Generar el reporte
        return generator.generateReport(parameters);
    }

    /**
     * Generar reporte de citas
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateAppointmentsReport(LocalDateTime startDate, LocalDateTime endDate) {
        return generateReport("APPOINTMENTS", startDate, endDate);
    }

    /**
     * Generar reporte de pacientes
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePatientsReport() {
        return generateReport("PATIENTS", null, null);
    }

    /**
     * Generar reporte de servicios
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateServicesReport() {
        return generateReport("SERVICES", null, null);
    }

    /**
     * Obtener información del formato de reporte
     */
    public String getFileExtension(String reportType) {
        ReportGenerator generator = reportFactory.create(reportType);
        return generator.getFileExtension();
    }

    /**
     * Obtener MIME type del formato
     */
    public String getMimeType(String reportType) {
        ReportGenerator generator = reportFactory.create(reportType);
        return generator.getMimeType();
    }
}



