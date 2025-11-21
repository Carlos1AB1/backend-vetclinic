package com.vetclinic.patterns.chain.appointment;

import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.entity.Patient;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Chain of Responsibility Pattern
 * Valida que el paciente existe y está activo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PatientValidationHandler extends AppointmentValidationHandler {

    private final PatientRepository patientRepository;

    @Override
    public ValidationResult validate(CreateAppointmentRequest request) {
        log.debug("Validando paciente para cita");

        if (request.getPatientId() == null) {
            return ValidationResult.error("El ID del paciente es requerido");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElse(null);

        if (patient == null) {
            return ValidationResult.error("El paciente no existe con ID: " + request.getPatientId());
        }

        if (!patient.getIsActive()) {
            return ValidationResult.error("El paciente no está activo");
        }

        log.debug("Validación de paciente exitosa");
        return checkNext(request);
    }
}

