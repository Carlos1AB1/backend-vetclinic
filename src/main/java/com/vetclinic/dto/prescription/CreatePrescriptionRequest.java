package com.vetclinic.dto.prescription;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para crear una nueva prescripción
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionRequest {

    @NotNull(message = "El ID del registro médico es requerido")
    private Long medicalRecordId;

    @NotNull(message = "El ID del paciente es requerido")
    private Long patientId;

    @NotBlank(message = "El nombre del medicamento es requerido")
    @Size(max = 200, message = "El nombre del medicamento no puede exceder 200 caracteres")
    private String medicationName;

    @NotBlank(message = "La dosis es requerida")
    @Size(max = 100, message = "La dosis no puede exceder 100 caracteres")
    private String dosage;

    @NotBlank(message = "La frecuencia es requerida")
    @Size(max = 100, message = "La frecuencia no puede exceder 100 caracteres")
    private String frequency;

    @NotBlank(message = "La duración es requerida")
    @Size(max = 100, message = "La duración no puede exceder 100 caracteres")
    private String duration;

    @Size(max = 5000, message = "Las instrucciones no pueden exceder 5000 caracteres")
    private String instructions;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime startDate;

    @Future(message = "La fecha de finalización debe ser futura")
    private LocalDateTime endDate;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;
}
