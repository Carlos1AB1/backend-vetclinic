package com.vetclinic.patterns.factory.report;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.entity.Appointment;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Factory Method Pattern
 * Generador de reporte de citas
 * RF015 - Reportes Operativos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentsReportGenerator implements ReportGenerator {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public ByteArrayOutputStream generateReport(Map<String, Object> parameters) {
        log.info("Generando reporte de citas");

        LocalDateTime startDate = (LocalDateTime) parameters.get("startDate");
        LocalDateTime endDate = (LocalDateTime) parameters.get("endDate");

        // Obtener citas usando el servicio que devuelve DTOs
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Citas");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Paciente", "Propietario", "Veterinario", "Fecha", "Tipo", "Estado", "Razón"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (AppointmentDTO appointment : appointments) {
                try {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(appointment.getId());
                    
                    // Manejar relaciones con verificación de null
                    String patientName = appointment.getPatientName() != null ? appointment.getPatientName() : "N/A";
                    String ownerName = appointment.getOwnerName() != null ? appointment.getOwnerName() : "N/A";
                    String veterinarianName = appointment.getVeterinarianName() != null ? appointment.getVeterinarianName() : "N/A";
                    
                    row.createCell(1).setCellValue(patientName);
                    row.createCell(2).setCellValue(ownerName);
                    row.createCell(3).setCellValue(veterinarianName);
                    row.createCell(4).setCellValue(appointment.getScheduledDate().format(DATE_FORMATTER));
                    row.createCell(5).setCellValue(appointment.getAppointmentType());
                    row.createCell(6).setCellValue(appointment.getStatus());
                    row.createCell(7).setCellValue(appointment.getReason() != null ? appointment.getReason() : "");
                } catch (Exception e) {
                    log.warn("Error al procesar cita ID {}: {}", appointment.getId(), e.getMessage());
                    // Continuar con la siguiente cita
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Reporte de citas generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte de citas", e);
            throw new RuntimeException("Error al generar reporte", e);
        }

        return outputStream;
    }

    @Override
    public String getReportType() {
        return "APPOINTMENTS";
    }

    @Override
    public String getFileExtension() {
        return ".xlsx";
    }

    @Override
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
}

