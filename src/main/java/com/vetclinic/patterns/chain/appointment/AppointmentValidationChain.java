package com.vetclinic.patterns.chain.appointment;

import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.patterns.chain.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Chain of Responsibility Pattern
 * Cadena de validación para citas
 * Valida: paciente, disponibilidad del veterinario
 */
@Component
@RequiredArgsConstructor
public class AppointmentValidationChain {

    private final PatientValidationHandler patientValidator;
    private final VeterinarianAvailabilityHandler veterinarianValidator;

    /**
     * Ejecutar todas las validaciones en cadena
     * 
     * @param request Request de creación de cita
     * @return Resultado de la validación
     */
    public ValidationResult validate(CreateAppointmentRequest request) {
        // Construir la cadena
        patientValidator.setNext(veterinarianValidator);
        
        // Ejecutar validación desde el primer handler
        return patientValidator.validate(request);
    }
}

