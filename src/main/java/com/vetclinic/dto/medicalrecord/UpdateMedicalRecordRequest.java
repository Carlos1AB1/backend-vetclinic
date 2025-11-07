package com.vetclinic.dto.medicalrecord;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para actualizar un registro médico existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMedicalRecordRequest {

    private Long patientId;

    private Long appointmentId;

    private UUID veterinarianId;

    @PastOrPresent(message = "La fecha del registro no puede ser futura")
    private LocalDateTime recordDate;

    @Size(max = 5000, message = "El diagnóstico no puede exceder 5000 caracteres")
    private String diagnosis;

    @Size(max = 5000, message = "El tratamiento no puede exceder 5000 caracteres")
    private String treatment;

    @Size(max = 5000, message = "Los síntomas no pueden exceder 5000 caracteres")
    private String symptoms;

    @Size(max = 2000, message = "Los signos vitales no pueden exceder 2000 caracteres")
    private String vitalSigns;

    @DecimalMin(value = "0.0", inclusive = false, message = "El peso debe ser mayor que 0")
    @DecimalMax(value = "1000.0", message = "El peso no puede exceder 1000 kg")
    private BigDecimal weight;

    @DecimalMin(value = "30.0", message = "La temperatura debe ser al menos 30°C")
    @DecimalMax(value = "45.0", message = "La temperatura no puede exceder 45°C")
    private BigDecimal temperature;

    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notes;

    private Boolean followUpRequired;

    @Future(message = "La fecha de seguimiento debe ser futura")
    private LocalDateTime followUpDate;

    private Boolean isActive;
}
