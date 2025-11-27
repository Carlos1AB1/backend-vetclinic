package com.vetclinic.service;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.service.ServiceDTO;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BadRequestException;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para el portal de propietarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OwnerPortalService {

    private final OwnerService ownerService;
    private final ClinicService clinicService;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /**
     * Obtener todas las citas del propietario
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getMyAppointments(String userId) {
        Owner owner = ownerService.getOwnerByUserId(userId);
        List<Appointment> appointments = appointmentRepository.findByOwnerIdOrderByScheduledDateDesc(owner.getId());
        return appointments.stream()
                .map(this::mapAppointmentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crear una cita para el propietario
     */
    public AppointmentDTO createAppointment(String userId, CreateAppointmentRequest request) {
        Owner owner = ownerService.getOwnerByUserId(userId);

        // Validar que el ownerId del request coincida con el owner autenticado
        if (!request.getOwnerId().equals(owner.getId())) {
            throw new BusinessException("No puede crear citas para otros propietarios");
        }

        // Validar que la mascota pertenece al propietario
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + request.getPatientId()));

        if (!patient.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("La mascota no pertenece a este propietario");
        }

        if (!patient.getIsActive()) {
            throw new BusinessException("La mascota no está activa");
        }

        // Validar que el veterinario existe
        User veterinarian = userRepository.findById(request.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarianId()));

        // Validar fecha futura
        if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la cita debe ser futura");
        }

        // Crear cita
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

        return mapAppointmentToDTO(savedAppointment);
    }

    /**
     * Cancelar una cita
     */
    public AppointmentDTO cancelAppointment(String userId, Long appointmentId) {
        Owner owner = ownerService.getOwnerByUserId(userId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        // Validar que la cita pertenece al propietario
        if (!appointment.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Esta cita no pertenece a este propietario");
        }

        // Validar que la cita puede ser cancelada
        if (!appointment.canBeCancelled()) {
            throw new BusinessException("Esta cita no puede ser cancelada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        log.info("Cita cancelada exitosamente: {}", appointmentId);
        return mapAppointmentToDTO(updatedAppointment);
    }

    /**
     * Reprogramar una cita
     */
    public AppointmentDTO rescheduleAppointment(String userId, Long appointmentId, CreateAppointmentRequest request) {
        Owner owner = ownerService.getOwnerByUserId(userId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        // Validar que la cita pertenece al propietario
        if (!appointment.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Esta cita no pertenece a este propietario");
        }

        // Validar que el ownerId del request coincida con el owner autenticado
        if (!request.getOwnerId().equals(owner.getId())) {
            throw new BusinessException("No puede reprogramar citas para otros propietarios");
        }

        // Validar que la cita puede ser reprogramada
        if (!appointment.canBeCancelled()) {
            throw new BusinessException("Esta cita no puede ser reprogramada");
        }

        // Validar fecha futura
        if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la cita debe ser futura");
        }

        // Validar veterinario
        User veterinarian = userRepository.findById(request.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarianId()));

        // Validar que la mascota pertenece al propietario
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + request.getPatientId()));

        if (!patient.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("La mascota no pertenece a este propietario");
        }

        appointment.setPatient(patient);
        appointment.setScheduledDate(request.getScheduledDate());
        appointment.setVeterinarian(veterinarian);
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Cita reprogramada exitosamente: {}", appointmentId);

        return mapAppointmentToDTO(updatedAppointment);
    }

    /**
     * Obtener mascotas del propietario
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> getMyPets(String userId) {
        Owner owner = ownerService.getOwnerByUserId(userId);
        List<Patient> patients = patientRepository.findByOwnerIdAndIsActiveTrue(owner.getId());
        return patients.stream()
                .map(this::mapPatientToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener servicios disponibles
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAvailableServices() {
        return clinicService.getAllActiveServices();
    }

    // Métodos de mapeo

    private AppointmentDTO mapAppointmentToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getName());
        dto.setOwnerId(appointment.getOwner().getId());
        dto.setOwnerName(appointment.getOwner().getFullName());
        dto.setVeterinarianId(UUID.fromString(appointment.getVeterinarian().getId().toString()));
        dto.setVeterinarianName(appointment.getVeterinarian().getFirstName() + " " + appointment.getVeterinarian().getLastName());
        dto.setScheduledDate(appointment.getScheduledDate());
        dto.setAppointmentType(appointment.getAppointmentType());
        dto.setStatus(appointment.getStatus().toString());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setIsActive(appointment.getIsActive());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }

    private PatientDTO mapPatientToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setSpecies(patient.getSpecies());
        dto.setBreed(patient.getBreed());
        dto.setBirthDate(patient.getBirthDate());
        dto.setAge(patient.getAge());
        dto.setGender(patient.getGender());
        dto.setColor(patient.getColor());
        dto.setWeight(patient.getWeight());
        dto.setMicrochipNumber(patient.getMicrochipNumber());
        dto.setAllergies(patient.getAllergies());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setIsActive(patient.getIsActive());
        dto.setNotes(patient.getNotes());
        dto.setOwnerId(patient.getOwner().getId());
        dto.setOwnerName(patient.getOwner().getFullName());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        return dto;
    }
}