package com.vetclinic.dto.prescription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de prescripciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {

    private Long id;
    
    // Información del registro médico
    private Long medicalRecordId;
    private LocalDateTime medicalRecordDate;
    
    // Información del paciente
    private Long patientId;
    private String patientName;
    
    // Información de la prescripción
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String notes;
    
    // Estado
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean isCurrentlyActive;
    
    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
