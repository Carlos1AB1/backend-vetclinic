package com.vetclinic.service;

import com.vetclinic.dto.medicalrecord.CreateMedicalRecordRequest;
import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.MedicalRecordRepository;
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
 * Servicio para la gestión de registros médicos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    /**
     * Crear un nuevo registro médico
     */
    public MedicalRecordDTO createMedicalRecord(CreateMedicalRecordRequest request) {
        log.info("Creando nuevo registro médico para paciente ID: {}", request.getPatientId());

        // Validar que el paciente existe
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getPatientId()));

        // Validar que el veterinario existe
        User veterinarian = userRepository.findById(request.getVeterinarianId())
            .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarianId()));

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setVeterinarian(veterinarian);
        medicalRecord.setRecordDate(request.getRecordDate());
        medicalRecord.setDiagnosis(request.getDiagnosis());
        medicalRecord.setTreatment(request.getTreatment());
        medicalRecord.setSymptoms(request.getSymptoms());
        medicalRecord.setVitalSigns(request.getVitalSigns());
        medicalRecord.setWeight(request.getWeight());
        medicalRecord.setTemperature(request.getTemperature());
        medicalRecord.setNotes(request.getNotes());
        medicalRecord.setFollowUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false);
        medicalRecord.setFollowUpDate(request.getFollowUpDate());
        medicalRecord.setIsActive(true);

        // Si hay una cita asociada, validarla
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + request.getAppointmentId()));
            medicalRecord.setAppointment(appointment);
        }

        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);
        log.info("Registro médico creado exitosamente con ID: {}", savedRecord.getId());

        return mapToDTO(savedRecord);
    }

    /**
     * Obtener registro médico por ID
     */
    @Transactional(readOnly = true)
    public MedicalRecordDTO getMedicalRecordById(Long id) {
        log.info("Buscando registro médico con ID: {}", id);
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Registro médico no encontrado con ID: " + id));
        return mapToDTO(medicalRecord);
    }

    /**
     * Obtener registros médicos con paginación
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDTO> getMedicalRecordsPage(Pageable pageable) {
        log.info("Obteniendo página de registros médicos: {}", pageable);
        return medicalRecordRepository.findByIsActiveTrueOrderByRecordDateDesc(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener registros médicos por paciente
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getMedicalRecordsByPatient(Long patientId) {
        log.info("Obteniendo registros médicos del paciente: {}", patientId);
        return medicalRecordRepository.findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(patientId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener registros médicos por veterinario
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getMedicalRecordsByVeterinarian(UUID veterinarianId) {
        log.info("Obteniendo registros médicos del veterinario: {}", veterinarianId);
        return medicalRecordRepository.findByVeterinarianIdAndIsActiveTrueOrderByRecordDateDesc(veterinarianId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener registros médicos por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getMedicalRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Obteniendo registros médicos entre {} y {}", startDate, endDate);
        return medicalRecordRepository.findByDateRange(startDate, endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Buscar registros por diagnóstico
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDTO> searchByDiagnosis(String diagnosis, Pageable pageable) {
        log.info("Buscando registros por diagnóstico: {}", diagnosis);
        return medicalRecordRepository.searchByDiagnosis(diagnosis, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener registros que requieren seguimiento
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getRecordsRequiringFollowUp() {
        log.info("Obteniendo registros que requieren seguimiento");
        return medicalRecordRepository.findRecordsRequiringFollowUp()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar registro médico
     */
    public MedicalRecordDTO updateMedicalRecord(Long id, UpdateMedicalRecordRequest request) {
        log.info("Actualizando registro médico con ID: {}", id);

        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Registro médico no encontrado con ID: " + id));

        // Actualizar paciente si cambió
        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
            medicalRecord.setPatient(patient);
        }

        // Actualizar veterinario si cambió
        if (request.getVeterinarianId() != null) {
            User veterinarian = userRepository.findById(request.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));
            medicalRecord.setVeterinarian(veterinarian);
        }

        // Actualizar cita si cambió
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
            medicalRecord.setAppointment(appointment);
        }

        // Actualizar otros campos
        if (request.getRecordDate() != null) medicalRecord.setRecordDate(request.getRecordDate());
        if (request.getDiagnosis() != null) medicalRecord.setDiagnosis(request.getDiagnosis());
        if (request.getTreatment() != null) medicalRecord.setTreatment(request.getTreatment());
        if (request.getSymptoms() != null) medicalRecord.setSymptoms(request.getSymptoms());
        if (request.getVitalSigns() != null) medicalRecord.setVitalSigns(request.getVitalSigns());
        if (request.getWeight() != null) medicalRecord.setWeight(request.getWeight());
        if (request.getTemperature() != null) medicalRecord.setTemperature(request.getTemperature());
        if (request.getNotes() != null) medicalRecord.setNotes(request.getNotes());
        if (request.getFollowUpRequired() != null) medicalRecord.setFollowUpRequired(request.getFollowUpRequired());
        if (request.getFollowUpDate() != null) medicalRecord.setFollowUpDate(request.getFollowUpDate());
        if (request.getIsActive() != null) medicalRecord.setIsActive(request.getIsActive());

        MedicalRecord updatedRecord = medicalRecordRepository.save(medicalRecord);
        log.info("Registro médico actualizado exitosamente");

        return mapToDTO(updatedRecord);
    }

    /**
     * Eliminar registro médico (soft delete)
     */
    public void deleteMedicalRecord(Long id) {
        log.info("Eliminando registro médico con ID: {}", id);

        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Registro médico no encontrado con ID: " + id));

        medicalRecord.setIsActive(false);
        medicalRecordRepository.save(medicalRecord);

        log.info("Registro médico eliminado exitosamente");
    }

    /**
     * Contar registros médicos activos
     */
    @Transactional(readOnly = true)
    public long countActiveMedicalRecords() {
        return medicalRecordRepository.countByIsActiveTrue();
    }

    /**
     * Mapear entidad a DTO
     */
    private MedicalRecordDTO mapToDTO(MedicalRecord medicalRecord) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(medicalRecord.getId());
        
        // Información del paciente
        if (medicalRecord.getPatient() != null) {
            dto.setPatientId(medicalRecord.getPatient().getId());
            dto.setPatientName(medicalRecord.getPatient().getName());
            dto.setPatientSpecies(medicalRecord.getPatient().getSpecies());
        }
        
        // Información de la cita
        if (medicalRecord.getAppointment() != null) {
            dto.setAppointmentId(medicalRecord.getAppointment().getId());
            dto.setAppointmentDate(medicalRecord.getAppointment().getScheduledDate());
        }
        
        // Información del veterinario
        if (medicalRecord.getVeterinarian() != null) {
            dto.setVeterinarianId(medicalRecord.getVeterinarian().getId());
            dto.setVeterinarianName(medicalRecord.getVeterinarian().getFirstName() + " " + 
                                   medicalRecord.getVeterinarian().getLastName());
        }
        
        dto.setRecordDate(medicalRecord.getRecordDate());
        dto.setDiagnosis(medicalRecord.getDiagnosis());
        dto.setTreatment(medicalRecord.getTreatment());
        dto.setSymptoms(medicalRecord.getSymptoms());
        dto.setVitalSigns(medicalRecord.getVitalSigns());
        dto.setWeight(medicalRecord.getWeight());
        dto.setTemperature(medicalRecord.getTemperature());
        dto.setNotes(medicalRecord.getNotes());
        dto.setFollowUpRequired(medicalRecord.getFollowUpRequired());
        dto.setFollowUpDate(medicalRecord.getFollowUpDate());
        dto.setIsActive(medicalRecord.getIsActive());
        dto.setPrescriptionCount(medicalRecord.getPrescriptions() != null ? 
                                medicalRecord.getPrescriptions().size() : 0);
        dto.setCreatedAt(medicalRecord.getCreatedAt());
        dto.setUpdatedAt(medicalRecord.getUpdatedAt());
        
        return dto;
    }
}
