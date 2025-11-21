package com.vetclinic.service;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.appointment.RescheduleAppointmentRequest;
import com.vetclinic.dto.appointment.UpdateAppointmentRequest;
import com.vetclinic.entity.AppointmentActionToken;
import com.vetclinic.entity.AppointmentActionToken.ActionType;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.patterns.chain.appointment.AppointmentValidationChain;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.patterns.observer.AppointmentEvent;
import com.vetclinic.patterns.state.AppointmentStateContext;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.OwnerRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.AppointmentActionTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de citas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final AppointmentValidationChain validationChain;
    private final AppointmentStateContext stateContext;
    private final ApplicationEventPublisher eventPublisher;
    private final AppointmentActionTokenService tokenService;

    /**
     * Crear una nueva cita
     */
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        log.info("Creando nueva cita para paciente ID: {}", request.getPatientId());

        // Validar usando Chain of Responsibility
        ValidationResult validationResult = validationChain.validate(request);
        if (!validationResult.isValid()) {
            throw new BusinessException(validationResult.getMessage());
        }

        // Validar que el propietario existe y está activo
        Owner owner = ownerRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + request.getOwnerId()));
        
        if (!owner.getIsActive()) {
            throw new BusinessException("El propietario no está activo");
        }

        // Obtener entidades necesarias
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getPatientId()));
        
        User veterinarian = userRepository.findById(request.getVeterinarianId())
            .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarianId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setOwner(owner);
        appointment.setVeterinarian(veterinarian);
        appointment.setScheduledDate(request.getScheduledDate());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        appointment.setIsActive(true);

        log.info("Guardando cita con fecha programada: {}", request.getScheduledDate());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Cita creada exitosamente con ID: {}, Fecha guardada: {}", savedAppointment.getId(), savedAppointment.getScheduledDate());

        // Publicar evento usando Observer Pattern
        AppointmentEvent event = new AppointmentEvent(
            this,
            savedAppointment,
            AppointmentEvent.AppointmentEventType.CREATED,
            null
        );
        eventPublisher.publishEvent(event);

        return mapToDTO(savedAppointment);
    }

    /**
     * Obtener cita por ID
     */
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        log.info("Buscando cita con ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));
        return mapToDTO(appointment);
    }

    /**
     * Obtener citas con paginación
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getAppointmentsPage(Pageable pageable) {
        log.info("Obteniendo página de citas: {}", pageable);
        return appointmentRepository.findByIsActiveTrueOrderByScheduledDateDesc(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener citas por paciente
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        log.info("Obteniendo citas del paciente: {}", patientId);
        return appointmentRepository.findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(patientId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener citas por propietario
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByOwner(Long ownerId) {
        log.info("Obteniendo citas del propietario: {}", ownerId);
        return appointmentRepository.findByOwnerIdAndIsActiveTrueOrderByScheduledDateDesc(ownerId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener citas por veterinario
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByVeterinarian(UUID veterinarianId) {
        log.info("Obteniendo citas del veterinario: {}", veterinarianId);
        return appointmentRepository.findByVeterinarianIdAndIsActiveTrueOrderByScheduledDateDesc(veterinarianId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener citas por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Obteniendo citas entre {} y {}", startDate, endDate);
        return appointmentRepository.findByDateRange(startDate, endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener citas próximas (7 días)
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(7);
        log.info("Obteniendo citas próximas");
        return appointmentRepository.findUpcomingAppointments(now, endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar cita
     */
    public AppointmentDTO updateAppointment(Long id, UpdateAppointmentRequest request) {
        log.info("Actualizando cita con ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        // Actualizar paciente si cambió
        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
            appointment.setPatient(patient);
        }

        // Actualizar propietario si cambió
        if (request.getOwnerId() != null) {
            Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));
            appointment.setOwner(owner);
        }

        // Actualizar veterinario si cambió
        if (request.getVeterinarianId() != null) {
            User veterinarian = userRepository.findById(request.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));
            appointment.setVeterinarian(veterinarian);
        }

        // Verificar conflicto si cambió la fecha o veterinario
        if (request.getScheduledDate() != null || request.getVeterinarianId() != null) {
            UUID vetId = request.getVeterinarianId() != null ? 
                        request.getVeterinarianId() : appointment.getVeterinarian().getId();
            LocalDateTime schedDate = request.getScheduledDate() != null ? 
                                     request.getScheduledDate() : appointment.getScheduledDate();
            
            if (appointmentRepository.existsConflictExcluding(vetId, schedDate, id)) {
                throw new BusinessException("Ya existe una cita programada para este veterinario en el horario seleccionado");
            }
            
            if (request.getScheduledDate() != null) {
                appointment.setScheduledDate(request.getScheduledDate());
            }
        }

        // Actualizar otros campos
        String previousStatus = appointment.getStatus().name();
        AppointmentEvent.AppointmentEventType eventType = null;
        
        if (request.getAppointmentType() != null) appointment.setAppointmentType(request.getAppointmentType());
        if (request.getStatus() != null) {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(request.getStatus());
            
            // Usar State Pattern para transiciones de estado
            try {
                if (newStatus == AppointmentStatus.CONFIRMED) {
                    stateContext.confirm(appointment);
                    eventType = AppointmentEvent.AppointmentEventType.CONFIRMED;
                } else if (newStatus == AppointmentStatus.COMPLETED) {
                    stateContext.complete(appointment);
                    eventType = AppointmentEvent.AppointmentEventType.COMPLETED;
                } else if (newStatus == AppointmentStatus.IN_PROGRESS) {
                    stateContext.start(appointment);
                    eventType = AppointmentEvent.AppointmentEventType.STATUS_CHANGED;
                } else {
                    appointment.setStatus(newStatus);
                    if (!previousStatus.equals(newStatus.name())) {
                        eventType = AppointmentEvent.AppointmentEventType.STATUS_CHANGED;
                    }
                }
            } catch (IllegalStateException e) {
                throw new BusinessException("Transición de estado inválida: " + e.getMessage());
            }
        }
        if (request.getReason() != null) appointment.setReason(request.getReason());
        if (request.getNotes() != null) appointment.setNotes(request.getNotes());
        if (request.getDurationMinutes() != null) appointment.setDurationMinutes(request.getDurationMinutes());
        if (request.getIsActive() != null) appointment.setIsActive(request.getIsActive());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Cita actualizada exitosamente");

        // Publicar evento si hubo cambio de estado
        if (eventType != null) {
            AppointmentEvent event = new AppointmentEvent(
                this,
                updatedAppointment,
                eventType,
                previousStatus
            );
            eventPublisher.publishEvent(event);
        }

        return mapToDTO(updatedAppointment);
    }

    /**
     * Cancelar cita usando State Pattern
     */
    public void cancelAppointment(Long id) {
        log.info("Cancelando cita con ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        String previousStatus = appointment.getStatus().name();

        // Usar State Pattern para cancelar
        if (!stateContext.canBeRescheduled(appointment)) {
            throw new BusinessException("Esta cita no puede ser cancelada");
        }

        stateContext.cancel(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Cita cancelada exitosamente");

        // Publicar evento usando Observer Pattern
        AppointmentEvent event = new AppointmentEvent(
            this,
            savedAppointment,
            AppointmentEvent.AppointmentEventType.CANCELLED,
            previousStatus
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * Eliminar cita (soft delete)
     */
    public void deleteAppointment(Long id) {
        log.info("Eliminando cita con ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        appointment.setIsActive(false);
        appointmentRepository.save(appointment);

        log.info("Cita eliminada exitosamente");
    }

    /**
     * Contar citas activas
     */
    @Transactional(readOnly = true)
    public long countActiveAppointments() {
        return appointmentRepository.countByIsActiveTrue();
    }

    /**
     * Confirmar cita desde recordatorio (RF018)
     */
    public AppointmentDTO confirmAppointmentFromReminder(Long appointmentId, String token) {
        log.info("Confirmando cita ID: {} desde recordatorio", appointmentId);

        // Validar token
        AppointmentActionToken actionToken = tokenService.validateToken(token);
        
        // Verificar que el token corresponde a la cita correcta
        if (!actionToken.getAppointment().getId().equals(appointmentId)) {
            throw new BusinessException("El token no corresponde a esta cita");
        }

        // Verificar que el token es para la acción correcta
        if (actionToken.getActionType() != ActionType.CONFIRM) {
            throw new BusinessException("Este token no es válido para confirmar la cita");
        }

        // Confirmar cita usando State Pattern
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        String previousStatus = appointment.getStatus().name();
        stateContext.confirm(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Marcar token como usado
        tokenService.markTokenAsUsed(token);

        // Publicar evento
        AppointmentEvent event = new AppointmentEvent(
                this,
                savedAppointment,
                AppointmentEvent.AppointmentEventType.CONFIRMED,
                previousStatus
        );
        eventPublisher.publishEvent(event);

        log.info("Cita confirmada exitosamente desde recordatorio");
        return mapToDTO(savedAppointment);
    }

    /**
     * Cancelar cita desde recordatorio (RF018)
     */
    public void cancelAppointmentFromReminder(Long appointmentId, String token) {
        log.info("Cancelando cita ID: {} desde recordatorio", appointmentId);

        // Validar token
        AppointmentActionToken actionToken = tokenService.validateToken(token);
        
        // Verificar que el token corresponde a la cita correcta
        if (!actionToken.getAppointment().getId().equals(appointmentId)) {
            throw new BusinessException("El token no corresponde a esta cita");
        }

        // Verificar que el token es para la acción correcta
        if (actionToken.getActionType() != ActionType.CANCEL) {
            throw new BusinessException("Este token no es válido para cancelar la cita");
        }

        // Cancelar cita usando State Pattern
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        String previousStatus = appointment.getStatus().name();
        
        if (!stateContext.canBeRescheduled(appointment)) {
            throw new BusinessException("Esta cita no puede ser cancelada");
        }

        stateContext.cancel(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Marcar token como usado
        tokenService.markTokenAsUsed(token);

        // Publicar evento
        AppointmentEvent event = new AppointmentEvent(
                this,
                savedAppointment,
                AppointmentEvent.AppointmentEventType.CANCELLED,
                previousStatus
        );
        eventPublisher.publishEvent(event);

        log.info("Cita cancelada exitosamente desde recordatorio");
    }

    /**
     * Reprogramar cita desde recordatorio (RF018)
     */
    public AppointmentDTO rescheduleAppointmentFromReminder(Long appointmentId, String token, RescheduleAppointmentRequest request) {
        log.info("Reprogramando cita ID: {} desde recordatorio", appointmentId);

        // Validar token
        AppointmentActionToken actionToken = tokenService.validateToken(token);
        
        // Verificar que el token corresponde a la cita correcta
        if (!actionToken.getAppointment().getId().equals(appointmentId)) {
            throw new BusinessException("El token no corresponde a esta cita");
        }

        // Verificar que el token es para la acción correcta
        if (actionToken.getActionType() != ActionType.RESCHEDULE) {
            throw new BusinessException("Este token no es válido para reprogramar la cita");
        }

        // Obtener cita
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        // Verificar que puede ser reprogramada
        if (!stateContext.canBeRescheduled(appointment)) {
            throw new BusinessException("Esta cita no puede ser reprogramada");
        }

        // Validar que no haya conflicto con la nueva fecha
        UUID veterinarianId = appointment.getVeterinarian().getId();
        if (appointmentRepository.existsConflictExcluding(veterinarianId, request.getNewScheduledDate(), appointmentId)) {
            throw new BusinessException("Ya existe una cita programada para este veterinario en el horario seleccionado");
        }

        // Actualizar fecha
        String previousStatus = appointment.getStatus().name();
        appointment.setScheduledDate(request.getNewScheduledDate());
        if (request.getReason() != null) {
            appointment.setReason(request.getReason());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Marcar token como usado
        tokenService.markTokenAsUsed(token);

        // Publicar evento
        AppointmentEvent event = new AppointmentEvent(
                this,
                savedAppointment,
                AppointmentEvent.AppointmentEventType.STATUS_CHANGED,
                previousStatus
        );
        eventPublisher.publishEvent(event);

        log.info("Cita reprogramada exitosamente desde recordatorio");
        return mapToDTO(savedAppointment);
    }

    /**
     * Mapear entidad a DTO
     */
    private AppointmentDTO mapToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        
        // Información del paciente
        if (appointment.getPatient() != null) {
            dto.setPatientId(appointment.getPatient().getId());
            dto.setPatientName(appointment.getPatient().getName());
            dto.setPatientSpecies(appointment.getPatient().getSpecies());
        }
        
        // Información del propietario
        if (appointment.getOwner() != null) {
            dto.setOwnerId(appointment.getOwner().getId());
            dto.setOwnerName(appointment.getOwner().getFullName());
            dto.setOwnerPhone(appointment.getOwner().getPhone());
        }
        
        // Información del veterinario
        if (appointment.getVeterinarian() != null) {
            dto.setVeterinarianId(appointment.getVeterinarian().getId());
            dto.setVeterinarianName(appointment.getVeterinarian().getFirstName() + " " + 
                                   appointment.getVeterinarian().getLastName());
        }
        
        dto.setScheduledDate(appointment.getScheduledDate());
        dto.setAppointmentType(appointment.getAppointmentType());
        dto.setStatus(appointment.getStatus().name());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setIsActive(appointment.getIsActive());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        
        return dto;
    }
}
