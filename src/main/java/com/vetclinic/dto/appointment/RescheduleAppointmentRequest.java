package com.vetclinic.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para reprogramar una cita desde recordatorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleAppointmentRequest {

    @NotNull(message = "La nueva fecha es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime newScheduledDate;

    @jakarta.validation.constraints.Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;
}

