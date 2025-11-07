package com.vetclinic.dto.appointment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para actualizar una cita existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private Long patientId;

    private Long ownerId;

    private UUID veterinarianId;

    @Future(message = "La fecha debe ser futura")
    private LocalDateTime scheduledDate;

    @Size(max = 50, message = "El tipo de cita no puede exceder 50 caracteres")
    @Pattern(regexp = "CONSULTATION|VACCINATION|SURGERY|CHECKUP|EMERGENCY", 
             message = "Tipo de cita inválido")
    private String appointmentType;

    @Pattern(regexp = "SCHEDULED|CONFIRMED|IN_PROGRESS|COMPLETED|CANCELLED|NO_SHOW",
             message = "Estado inválido")
    private String status;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Max(value = 480, message = "La duración máxima es 480 minutos (8 horas)")
    private Integer durationMinutes;

    private Boolean isActive;
}
