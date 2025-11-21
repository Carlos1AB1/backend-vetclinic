package com.vetclinic.patterns.factory.report;

import com.vetclinic.entity.Patient;
import com.vetclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Factory Method Pattern
 * Generador de reporte de pacientes atendidos
 * RF015 - Reportes Operativos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PatientsReportGenerator implements ReportGenerator {

    private final PatientRepository patientRepository;

    @Override
    public ByteArrayOutputStream generateReport(Map<String, Object> parameters) {
        log.info("Generando reporte de pacientes atendidos");

        // Usar método con JOIN FETCH para evitar problemas de lazy loading
        List<Patient> patients = patientRepository.findByIsActiveTrueWithOwner();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Pacientes");

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
            String[] headers = {"ID", "Nombre", "Especie", "Raza", "Propietario", "Edad", "Peso"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (Patient patient : patients) {
                try {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(patient.getId());
                    row.createCell(1).setCellValue(patient.getName());
                    row.createCell(2).setCellValue(patient.getSpecies());
                    row.createCell(3).setCellValue(patient.getBreed() != null ? patient.getBreed() : "N/A");
                    
                    // Manejar relación con propietario
                    String ownerName = patient.getOwner() != null ? patient.getOwner().getFullName() : "N/A";
                    row.createCell(4).setCellValue(ownerName);
                    
                    if (patient.getBirthDate() != null) {
                        long age = java.time.temporal.ChronoUnit.YEARS.between(patient.getBirthDate(), java.time.LocalDate.now());
                        row.createCell(5).setCellValue(age);
                    } else {
                        row.createCell(5).setCellValue("N/A");
                    }
                    
                    if (patient.getWeight() != null) {
                        row.createCell(6).setCellValue(patient.getWeight().doubleValue());
                    } else {
                        row.createCell(6).setCellValue("N/A");
                    }
                } catch (Exception e) {
                    log.warn("Error al procesar paciente ID {}: {}", patient.getId(), e.getMessage());
                    // Continuar con el siguiente paciente
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Reporte de pacientes generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte de pacientes", e);
            throw new RuntimeException("Error al generar reporte", e);
        }

        return outputStream;
    }

    @Override
    public String getReportType() {
        return "PATIENTS";
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

