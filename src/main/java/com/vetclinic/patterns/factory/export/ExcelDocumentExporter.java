package com.vetclinic.patterns.factory.export;

import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Prescription;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Factory Method Pattern
 * Exportador de documentos en formato Excel
 * RF014 - Generación de Recetas
 */
@Component
@Slf4j
public class ExcelDocumentExporter implements DocumentExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public ByteArrayOutputStream exportPrescription(Prescription prescription, MedicalRecord medicalRecord) {
        log.info("Exportando receta ID: {} a Excel", prescription.getId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Receta Médica");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RECETA MÉDICA VETERINARIA");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            int rowNum = 2;

            // Información del paciente
            Row patientHeaderRow = sheet.createRow(rowNum++);
            Cell patientHeaderCell = patientHeaderRow.createCell(0);
            patientHeaderCell.setCellValue("DATOS DEL PACIENTE");
            patientHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            createLabelValueRow(sheet, rowNum++, "Paciente:", prescription.getPatient().getName(), labelStyle);
            createLabelValueRow(sheet, rowNum++, "Especie:", prescription.getPatient().getSpecies(), labelStyle);
            if (prescription.getPatient().getBreed() != null) {
                createLabelValueRow(sheet, rowNum++, "Raza:", prescription.getPatient().getBreed(), labelStyle);
            }
            createLabelValueRow(sheet, rowNum++, "Propietario:", prescription.getPatient().getOwner().getFullName(), labelStyle);

            rowNum++;

            // Información de la receta
            Row prescriptionHeaderRow = sheet.createRow(rowNum++);
            Cell prescriptionHeaderCell = prescriptionHeaderRow.createCell(0);
            prescriptionHeaderCell.setCellValue("PRESCRIPCIÓN MÉDICA");
            prescriptionHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            createLabelValueRow(sheet, rowNum++, "Medicamento:", prescription.getMedicationName(), labelStyle);
            createLabelValueRow(sheet, rowNum++, "Dosis:", prescription.getDosage(), labelStyle);
            createLabelValueRow(sheet, rowNum++, "Frecuencia:", prescription.getFrequency(), labelStyle);
            createLabelValueRow(sheet, rowNum++, "Duración:", prescription.getDuration(), labelStyle);
            createLabelValueRow(sheet, rowNum++, "Fecha de inicio:", 
                prescription.getStartDate().format(DATE_FORMATTER), labelStyle);
            if (prescription.getEndDate() != null) {
                createLabelValueRow(sheet, rowNum++, "Fecha de fin:", 
                    prescription.getEndDate().format(DATE_FORMATTER), labelStyle);
            }

            if (prescription.getInstructions() != null && !prescription.getInstructions().isBlank()) {
                rowNum++;
                createLabelValueRow(sheet, rowNum++, "Instrucciones:", prescription.getInstructions(), labelStyle);
            }

            // Ajustar ancho de columnas
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 12000);

            workbook.write(outputStream);
            log.info("Receta exportada exitosamente a Excel");

        } catch (Exception e) {
            log.error("Error al exportar receta a Excel", e);
            throw new RuntimeException("Error al generar Excel de receta", e);
        }

        return outputStream;
    }

    private void createLabelValueRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value : "N/A");
    }

    @Override
    public String getFormat() {
        return "EXCEL";
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



