package com.vetclinic.patterns.chain.appointment;

import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.entity.User;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Chain of Responsibility Pattern
 * Valida que el veterinario existe y está disponible en el horario solicitado
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VeterinarianAvailabilityHandler extends AppointmentValidationHandler {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public ValidationResult validate(CreateAppointmentRequest request) {
        log.debug("Validando disponibilidad del veterinario");

        if (request.getVeterinarianId() == null) {
            return ValidationResult.error("El ID del veterinario es requerido");
        }

        if (request.getScheduledDate() == null) {
            return ValidationResult.error("La fecha de la cita es requerida");
        }

        // Validar que el veterinario existe
        User veterinarian = userRepository.findById(request.getVeterinarianId())
            .orElse(null);

        if (veterinarian == null) {
            return ValidationResult.error("El veterinario no existe con ID: " + request.getVeterinarianId());
        }

        if (!veterinarian.getIsActive()) {
            return ValidationResult.error("El veterinario no está activo");
        }

        // Validar que no haya conflicto de horario
        if (appointmentRepository.existsConflict(request.getVeterinarianId(), request.getScheduledDate())) {
            return ValidationResult.error("El veterinario ya tiene una cita programada en este horario");
        }

        log.debug("Validación de disponibilidad del veterinario exitosa");
        return checkNext(request);
    }
}

