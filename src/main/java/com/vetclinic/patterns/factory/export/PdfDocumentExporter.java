package com.vetclinic.patterns.factory.export;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Prescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Factory Method Pattern
 * Exportador de documentos en formato PDF
 * RF014 - Generación de Recetas
 */
@Component
@Slf4j
public class PdfDocumentExporter implements DocumentExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public ByteArrayOutputStream exportPrescription(Prescription prescription, MedicalRecord medicalRecord) {
        log.info("Exportando receta ID: {} a PDF", prescription.getId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph title = new Paragraph("RECETA MÉDICA VETERINARIA")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(title);

            // Información de la clínica
            Paragraph clinicInfo = new Paragraph("VetClinic Pro\nSistema de Gestión Veterinaria")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(clinicInfo);

            // Información del paciente
            Paragraph patientTitle = new Paragraph("DATOS DEL PACIENTE")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
            document.add(patientTitle);

            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            patientTable.addCell("Paciente:");
            patientTable.addCell(prescription.getPatient().getName());
            patientTable.addCell("Especie:");
            patientTable.addCell(prescription.getPatient().getSpecies());
            patientTable.addCell("Raza:");
            patientTable.addCell(prescription.getPatient().getBreed() != null ? prescription.getPatient().getBreed() : "N/A");
            patientTable.addCell("Propietario:");
            patientTable.addCell(prescription.getPatient().getOwner().getFullName());
            
            document.add(patientTable);

            // Información de la receta
            Paragraph prescriptionTitle = new Paragraph("PRESCRIPCIÓN MÉDICA")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
            document.add(prescriptionTitle);

            Table prescriptionTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            prescriptionTable.addCell("Medicamento:");
            prescriptionTable.addCell(prescription.getMedicationName());
            prescriptionTable.addCell("Dosis:");
            prescriptionTable.addCell(prescription.getDosage());
            prescriptionTable.addCell("Frecuencia:");
            prescriptionTable.addCell(prescription.getFrequency());
            prescriptionTable.addCell("Duración:");
            prescriptionTable.addCell(prescription.getDuration());
            prescriptionTable.addCell("Fecha de inicio:");
            prescriptionTable.addCell(prescription.getStartDate().format(DATE_FORMATTER));
            if (prescription.getEndDate() != null) {
                prescriptionTable.addCell("Fecha de fin:");
                prescriptionTable.addCell(prescription.getEndDate().format(DATE_FORMATTER));
            }
            
            document.add(prescriptionTable);

            // Instrucciones
            if (prescription.getInstructions() != null && !prescription.getInstructions().isBlank()) {
                Paragraph instructionsTitle = new Paragraph("INSTRUCCIONES")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10);
                document.add(instructionsTitle);

                Paragraph instructions = new Paragraph(prescription.getInstructions())
                    .setMarginBottom(20);
                document.add(instructions);
            }

            // Notas
            if (prescription.getNotes() != null && !prescription.getNotes().isBlank()) {
                Paragraph notesTitle = new Paragraph("NOTAS ADICIONALES")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10);
                document.add(notesTitle);

                Paragraph notes = new Paragraph(prescription.getNotes())
                    .setMarginBottom(20);
                document.add(notes);
            }

            // Diagnóstico del registro médico
            if (medicalRecord != null && medicalRecord.getDiagnosis() != null) {
                Paragraph diagnosisTitle = new Paragraph("DIAGNÓSTICO")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10);
                document.add(diagnosisTitle);

                Paragraph diagnosis = new Paragraph(medicalRecord.getDiagnosis())
                    .setMarginBottom(20);
                document.add(diagnosis);
            }

            // Información del veterinario
            Paragraph vetInfo = new Paragraph(
                String.format("Veterinario: %s\nFecha: %s",
                    medicalRecord != null && medicalRecord.getVeterinarian() != null 
                        ? medicalRecord.getVeterinarian().getFullName() 
                        : "N/A",
                    prescription.getCreatedAt() != null 
                        ? prescription.getCreatedAt().format(DATE_FORMATTER) 
                        : "N/A")
            )
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(40);
            document.add(vetInfo);

            document.close();
            log.info("Receta exportada exitosamente a PDF");

        } catch (Exception e) {
            log.error("Error al exportar receta a PDF", e);
            throw new RuntimeException("Error al generar PDF de receta", e);
        }

        return outputStream;
    }

    @Override
    public String getFormat() {
        return "PDF";
    }

    @Override
    public String getFileExtension() {
        return ".pdf";
    }

    @Override
    public String getMimeType() {
        return "application/pdf";
    }
}



