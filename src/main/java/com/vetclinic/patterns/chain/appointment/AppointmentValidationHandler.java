package com.vetclinic.patterns.chain.appointment;

import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.patterns.chain.ValidationResult;

/**
 * Chain of Responsibility Pattern
 * Handler base para validación de citas
 */
public abstract class AppointmentValidationHandler {
    
    protected AppointmentValidationHandler next;

    public AppointmentValidationHandler setNext(AppointmentValidationHandler next) {
        this.next = next;
        return next;
    }

    public abstract ValidationResult validate(CreateAppointmentRequest request);

    protected ValidationResult checkNext(CreateAppointmentRequest request) {
        if (next == null) {
            return ValidationResult.success();
        }
        return next.validate(request);
    }
}

