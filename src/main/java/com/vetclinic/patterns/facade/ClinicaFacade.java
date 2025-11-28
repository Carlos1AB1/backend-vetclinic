package com.vetclinic.patterns.facade;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.patterns.chain.appointment.AppointmentValidationChain;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.patterns.state.AppointmentStateContext;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.OwnerRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Facade Pattern
 * Facade que coordina múltiples servicios para operaciones complejas de la clínica
 * Simplifica la interfaz para los controladores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicaFacade {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final AppointmentValidationChain validationChain;
    private final AppointmentStateContext stateContext;

    /**
     * Agendar una cita completa (coordina validación, creación, notificaciones, etc.)
     * 
     * @param request Request de creación de cita
     * @return DTO de la cita creada
     */
    @Transactional
    public AppointmentDTO agendarCita(CreateAppointmentRequest request) {
        log.info("FACADE.agendarCita() - INICIANDO - Thread: {}", Thread.currentThread().getName());

        // 1. Validar la solicitud usando Chain of Responsibility
        ValidationResult validationResult = validationChain.validate(request);
        if (!validationResult.isValid()) {
            throw new com.vetclinic.exception.BusinessException(validationResult.getMessage());
        }

        // 2. Crear la cita usando el servicio (el servicio ya publica el evento CREATED UNA SOLA VEZ)
        log.info("FACADE: Llamando a appointmentService.createAppointment()");
        AppointmentDTO appointmentDTO = appointmentService.createAppointment(request);
        log.info("FACADE: appointmentService.createAppointment() completado - ID: {}", appointmentDTO.getId());

        log.info("FACADE: Cita agendada exitosamente con ID: {}", appointmentDTO.getId());
        return appointmentDTO;
    }

    /**
     * Confirmar una cita (coordina actualización, notificaciones, agenda)
     * 
     * @param appointmentId ID de la cita
     * @return DTO de la cita confirmada
     */
    @Transactional
    public AppointmentDTO confirmarCita(Long appointmentId) {
        log.info("FACADE: Confirmando cita ID: {}", appointmentId);

        // Actualizar estado usando el servicio (el servicio ya publica el evento CONFIRMED)
        com.vetclinic.dto.appointment.UpdateAppointmentRequest updateRequest = 
            new com.vetclinic.dto.appointment.UpdateAppointmentRequest();
        updateRequest.setStatus("CONFIRMED");
        AppointmentDTO appointmentDTO = appointmentService.updateAppointment(appointmentId, updateRequest);

        log.info("FACADE: Cita confirmada exitosamente");
        return appointmentDTO;
    }

    /**
     * Cancelar una cita (coordina actualización, notificaciones, liberación de recursos)
     * 
     * @param appointmentId ID de la cita
     */
    @Transactional
    public void cancelarCita(Long appointmentId) {
        log.info("FACADE: Cancelando cita ID: {}", appointmentId);

        // Cancelar usando el servicio (el servicio ya publica el evento CANCELLED)
        appointmentService.cancelAppointment(appointmentId);

        log.info("FACADE: Cita cancelada exitosamente");
    }

    /**
     * Completar una cita (coordina actualización, registro en historial, notificaciones)
     * 
     * @param appointmentId ID de la cita
     * @return DTO de la cita completada
     */
    @Transactional
    public AppointmentDTO completarCita(Long appointmentId) {
        log.info("FACADE: Completando cita ID: {}", appointmentId);

        // Actualizar estado usando el servicio (el servicio ya publica el evento COMPLETED)
        com.vetclinic.dto.appointment.UpdateAppointmentRequest updateRequest = 
            new com.vetclinic.dto.appointment.UpdateAppointmentRequest();
        updateRequest.setStatus("COMPLETED");
        AppointmentDTO appointmentDTO = appointmentService.updateAppointment(appointmentId, updateRequest);

        log.info("FACADE: Cita completada exitosamente");
        return appointmentDTO;
    }
}

