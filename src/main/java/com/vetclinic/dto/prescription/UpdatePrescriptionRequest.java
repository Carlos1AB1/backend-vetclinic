package com.vetclinic.dto.prescription;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para actualizar una prescripción existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrescriptionRequest {

    @Size(max = 200, message = "El nombre del medicamento no puede exceder 200 caracteres")
    private String medicationName;

    @Size(max = 100, message = "La dosis no puede exceder 100 caracteres")
    private String dosage;

    @Size(max = 100, message = "La frecuencia no puede exceder 100 caracteres")
    private String frequency;

    @Size(max = 100, message = "La duración no puede exceder 100 caracteres")
    private String duration;

    @Size(max = 5000, message = "Las instrucciones no pueden exceder 5000 caracteres")
    private String instructions;

    private LocalDateTime startDate;

    @Future(message = "La fecha de finalización debe ser futura")
    private LocalDateTime endDate;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    private Boolean isActive;
}
