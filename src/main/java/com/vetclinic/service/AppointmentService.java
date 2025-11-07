package com.vetclinic.service;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.appointment.UpdateAppointmentRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.OwnerRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Crear una nueva cita
     */
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        log.info("Creando nueva cita para paciente ID: {}", request.getPatientId());

        // Validar que el paciente existe y está activo
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getPatientId()));
        
        if (!patient.getIsActive()) {
            throw new BusinessException("El paciente no está activo");
        }

        // Validar que el propietario existe y está activo
        Owner owner = ownerRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + request.getOwnerId()));
        
        if (!owner.getIsActive()) {
            throw new BusinessException("El propietario no está activo");
        }

        // Validar que el veterinario existe
        User veterinarian = userRepository.findById(request.getVeterinarianId())
            .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarianId()));

        // Verificar conflicto de horario
        if (appointmentRepository.existsConflict(request.getVeterinarianId(), request.getScheduledDate())) {
            throw new BusinessException("Ya existe una cita programada para este veterinario en el horario seleccionado");
        }

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

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Cita creada exitosamente con ID: {}", savedAppointment.getId());

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
        if (request.getAppointmentType() != null) appointment.setAppointmentType(request.getAppointmentType());
        if (request.getStatus() != null) appointment.setStatus(AppointmentStatus.valueOf(request.getStatus()));
        if (request.getReason() != null) appointment.setReason(request.getReason());
        if (request.getNotes() != null) appointment.setNotes(request.getNotes());
        if (request.getDurationMinutes() != null) appointment.setDurationMinutes(request.getDurationMinutes());
        if (request.getIsActive() != null) appointment.setIsActive(request.getIsActive());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Cita actualizada exitosamente");

        return mapToDTO(updatedAppointment);
    }

    /**
     * Cancelar cita
     */
    public void cancelAppointment(Long id) {
        log.info("Cancelando cita con ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        if (!appointment.canBeCancelled()) {
            throw new BusinessException("Esta cita no puede ser cancelada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        log.info("Cita cancelada exitosamente");
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
