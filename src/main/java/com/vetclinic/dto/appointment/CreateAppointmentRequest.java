package com.vetclinic.dto.appointment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para crear una nueva cita
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull(message = "El ID del paciente es requerido")
    private Long patientId;

    @NotNull(message = "El ID del propietario es requerido")
    private Long ownerId;

    @NotNull(message = "El ID del veterinario es requerido")
    private UUID veterinarianId;

    @NotNull(message = "La fecha programada es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime scheduledDate;

    @NotBlank(message = "El tipo de cita es requerido")
    @Size(max = 50, message = "El tipo de cita no puede exceder 50 caracteres")
    @Pattern(regexp = "CONSULTATION|VACCINATION|SURGERY|CHECKUP|EMERGENCY", 
             message = "Tipo de cita inválido")
    private String appointmentType;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Max(value = 480, message = "La duración máxima es 480 minutos (8 horas)")
    private Integer durationMinutes;
}
