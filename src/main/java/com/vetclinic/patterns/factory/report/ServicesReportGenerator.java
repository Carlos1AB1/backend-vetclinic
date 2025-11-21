package com.vetclinic.patterns.factory.report;

import com.vetclinic.entity.Service;
import com.vetclinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Factory Method Pattern
 * Generador de reporte de servicios
 * RF015 - Reportes Operativos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServicesReportGenerator implements ReportGenerator {

    private final ServiceRepository serviceRepository;

    @Override
    public ByteArrayOutputStream generateReport(Map<String, Object> parameters) {
        log.info("Generando reporte de servicios");

        // Obtener todos los servicios activos usando el método del repositorio
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Service> services = serviceRepository.findByIsActiveTrueOrderByNameAsc(pageable).getContent();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Servicios");

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
            String[] headers = {"ID", "Nombre", "Categoría", "Precio", "Duración (min)", "Requiere Cita"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (Service service : services) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(service.getId());
                row.createCell(1).setCellValue(service.getName());
                row.createCell(2).setCellValue(service.getCategory());
                row.createCell(3).setCellValue(service.getPrice().doubleValue());
                row.createCell(4).setCellValue(service.getDurationMinutes());
                row.createCell(5).setCellValue(service.getRequiresAppointment() ? "Sí" : "No");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Reporte de servicios generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte de servicios", e);
            throw new RuntimeException("Error al generar reporte", e);
        }

        return outputStream;
    }

    @Override
    public String getReportType() {
        return "SERVICES";
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

