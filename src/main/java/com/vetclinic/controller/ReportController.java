package com.vetclinic.controller;

import com.vetclinic.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

/**
 * Controlador REST para generación de reportes
 * RF015 - Reportes Operativos
 * Usa Factory Method Pattern para diferentes tipos de reportes
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@io.swagger.v3.oas.annotations.tags.Tag(name = "12. Reportes Operativos", description = "Generación de reportes y análisis - Citas, pacientes, servicios en PDF")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    /**
     * Generar reporte según el tipo
     * GET /api/reports/generate?type=APPOINTMENTS&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("GET /api/reports/generate - Tipo: {}, StartDate: {}, EndDate: {}", type, startDate, endDate);
        
        // Usar Factory Method Pattern para generar el reporte
        ByteArrayOutputStream outputStream = reportService.generateReport(type, startDate, endDate);
        
        String fileExtension = reportService.getFileExtension(type);
        String mimeType = reportService.getMimeType(type);
        String filename = "reporte_" + type.toLowerCase() + "_" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + fileExtension;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    /**
     * Generar reporte de citas
     * GET /api/reports/appointments?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<byte[]> generateAppointmentsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("GET /api/reports/appointments - StartDate: {}, EndDate: {}", startDate, endDate);
        
        ByteArrayOutputStream outputStream = reportService.generateAppointmentsReport(startDate, endDate);
        
        String filename = "reporte_citas_" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    /**
     * Generar reporte de pacientes
     * GET /api/reports/patients
     */
    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<byte[]> generatePatientsReport() {
        log.info("GET /api/reports/patients - Generando reporte de pacientes");
        
        ByteArrayOutputStream outputStream = reportService.generatePatientsReport();
        
        String filename = "reporte_pacientes_" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    /**
     * Generar reporte de servicios
     * GET /api/reports/services
     */
    @GetMapping("/services")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<byte[]> generateServicesReport() {
        log.info("GET /api/reports/services - Generando reporte de servicios");
        
        ByteArrayOutputStream outputStream = reportService.generateServicesReport();
        
        String filename = "reporte_servicios_" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}



