package com.vetclinic.patterns.factory;

import com.vetclinic.patterns.factory.report.AppointmentsReportGenerator;
import com.vetclinic.patterns.factory.report.PatientsReportGenerator;
import com.vetclinic.patterns.factory.report.ReportGenerator;
import com.vetclinic.patterns.factory.report.ServicesReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern
 * Factory para crear diferentes tipos de generadores de reportes
 * RF015 - Reportes Operativos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportFactory {

    private final AppointmentsReportGenerator appointmentsReportGenerator;
    private final PatientsReportGenerator patientsReportGenerator;
    private final ServicesReportGenerator servicesReportGenerator;

    /**
     * Crear un generador de reportes según el tipo especificado
     * 
     * @param reportType Tipo de reporte: APPOINTMENTS, PATIENTS, SERVICES
     * @return Generador de reportes correspondiente
     */
    public ReportGenerator create(String reportType) {
        if (reportType == null || reportType.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de reporte no puede ser nulo o vacío");
        }

        String normalizedType = reportType.toUpperCase().trim();

        return switch (normalizedType) {
            case "APPOINTMENTS", "CITAS" -> {
                log.debug("Creando generador de reporte de citas");
                yield appointmentsReportGenerator;
            }
            case "PATIENTS", "PACIENTES" -> {
                log.debug("Creando generador de reporte de pacientes");
                yield patientsReportGenerator;
            }
            case "SERVICES", "SERVICIOS" -> {
                log.debug("Creando generador de reporte de servicios");
                yield servicesReportGenerator;
            }
            default -> {
                log.warn("Tipo de reporte no soportado: {}. Usando APPOINTMENTS por defecto", reportType);
                yield appointmentsReportGenerator;
            }
        };
    }

    /**
     * Crear generador de reportes por defecto (citas)
     */
    public ReportGenerator createDefault() {
        return appointmentsReportGenerator;
    }
}



