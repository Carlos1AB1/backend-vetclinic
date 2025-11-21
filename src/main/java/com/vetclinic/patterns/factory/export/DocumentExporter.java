package com.vetclinic.patterns.factory.export;

import com.vetclinic.entity.Prescription;
import com.vetclinic.entity.MedicalRecord;

import java.io.ByteArrayOutputStream;

/**
 * Factory Method Pattern
 * Interfaz para exportadores de documentos
 * RF014 - Generación de Recetas
 */
public interface DocumentExporter {
    
    /**
     * Exportar receta a formato específico
     * 
     * @param prescription Prescripción a exportar
     * @param medicalRecord Registro médico asociado
     * @return Bytes del documento generado
     */
    ByteArrayOutputStream exportPrescription(Prescription prescription, MedicalRecord medicalRecord);
    
    /**
     * Obtener el tipo de formato
     */
    String getFormat();
    
    /**
     * Obtener la extensión del archivo
     */
    String getFileExtension();
    
    /**
     * Obtener el MIME type
     */
    String getMimeType();
}



