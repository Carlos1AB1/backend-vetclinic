package com.vetclinic.service;

import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Prescription;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.patterns.factory.DocumentExportFactory;
import com.vetclinic.patterns.factory.export.DocumentExporter;
import com.vetclinic.repository.MedicalRecordRepository;
import com.vetclinic.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

/**
 * Servicio para exportar recetas en diferentes formatos
 * RF014 - Generación de Recetas
 * Usa Factory Method Pattern para crear exportadores según el formato
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionExportService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DocumentExportFactory documentExportFactory;

    /**
     * Exportar receta en formato específico
     * 
     * @param prescriptionId ID de la prescripción
     * @param format Formato: PDF, EXCEL
     * @return Bytes del documento generado
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportPrescription(Long prescriptionId, String format) {
        log.info("Exportando receta ID: {} en formato: {}", prescriptionId, format);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Prescripción no encontrada con ID: " + prescriptionId));

        MedicalRecord medicalRecord = prescription.getMedicalRecord();

        // Usar Factory Method Pattern para crear el exportador correcto
        DocumentExporter exporter = documentExportFactory.create(format);

        // Exportar la receta
        return exporter.exportPrescription(prescription, medicalRecord);
    }

    /**
     * Exportar receta en formato PDF (por defecto)
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportPrescriptionAsPdf(Long prescriptionId) {
        return exportPrescription(prescriptionId, "PDF");
    }

    /**
     * Obtener información del formato de exportación
     */
    public String getFileExtension(String format) {
        DocumentExporter exporter = documentExportFactory.create(format);
        return exporter.getFileExtension();
    }

    /**
     * Obtener MIME type del formato
     */
    public String getMimeType(String format) {
        DocumentExporter exporter = documentExportFactory.create(format);
        return exporter.getMimeType();
    }
}



