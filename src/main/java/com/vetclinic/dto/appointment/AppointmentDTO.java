package com.vetclinic.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuestas de citas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private Long id;
    
    // Información del paciente
    private Long patientId;
    private String patientName;
    private String patientSpecies;
    
    // Información del propietario
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    
    // Información del veterinario
    private UUID veterinarianId;
    private String veterinarianName;
    
    // Información de la cita
    private LocalDateTime scheduledDate;
    private String appointmentType;
    private String status;
    private String reason;
    private String notes;
    private Integer durationMinutes;
    private Boolean isActive;
    
    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
