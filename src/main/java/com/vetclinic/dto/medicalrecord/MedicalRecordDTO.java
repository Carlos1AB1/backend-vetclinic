package com.vetclinic.dto.medicalrecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuestas de registros médicos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {

    private Long id;
    
    // Información del paciente
    private Long patientId;
    private String patientName;
    private String patientSpecies;
    
    // Información de la cita
    private Long appointmentId;
    private LocalDateTime appointmentDate;
    
    // Información del veterinario
    private UUID veterinarianId;
    private String veterinarianName;
    
    // Información del registro
    private LocalDateTime recordDate;
    private String diagnosis;
    private String treatment;
    private String symptoms;
    private String vitalSigns;
    private BigDecimal weight;
    private BigDecimal temperature;
    private BigDecimal heartRate;
    private String notes;
    
    // Seguimiento
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;
    
    // Estado
    private Boolean isActive;
    
    // Contadores
    private Integer prescriptionCount;
    private Integer informedConsentCount;
    
    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
