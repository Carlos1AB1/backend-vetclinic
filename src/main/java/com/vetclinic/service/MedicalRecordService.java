package com.vetclinic.service;

import com.vetclinic.dto.medicalrecord.CreateMedicalRecordRequest;
import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.patterns.builder.MedicalRecordBuilder;
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

        // Usar Builder Pattern para construir el MedicalRecord
        MedicalRecordBuilder builder = new MedicalRecordBuilder()
            .conPaciente(patient)
            .conVeterinario(veterinarian)
            .conFechaRegistro(request.getRecordDate())
            .conDiagnostico(request.getDiagnosis())
            .conTratamiento(request.getTreatment())
            .conSintomas(request.getSymptoms())
            .conSignosVitales(request.getVitalSigns())
            .conPeso(request.getWeight())
            .conTemperatura(request.getTemperature())
            .conFrecuenciaCardiaca(request.getHeartRate())
            .conNotas(request.getNotes())
            .requiereSeguimiento(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false)
            .conFechaSeguimiento(request.getFollowUpDate())
            .activo(true);

        // Si hay una cita asociada, validarla y agregarla
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + request.getAppointmentId()));
            builder.conCita(appointment);
        }

        // Construir el objeto usando Builder
        MedicalRecord medicalRecord = builder.build();
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
        if (request.getHeartRate() != null) medicalRecord.setHeartRate(request.getHeartRate());
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
     * Crear o actualizar historia clínica automáticamente cuando se crea una cita
     * SIEMPRE usa la misma historia clínica por paciente (una historia clínica por paciente)
     * Si el paciente ya tiene historia clínica activa, se actualiza agregando la cita
     * Si no tiene, se crea una nueva historia clínica con la cita
     */
    public MedicalRecordDTO createOrUpdateMedicalRecordFromAppointment(Long appointmentId) {
        log.info("Creando o actualizando historia clínica para cita ID: {}", appointmentId);

        // Obtener la cita
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        Patient patient = appointment.getPatient();
        User veterinarian = appointment.getVeterinarian();

        // Buscar la historia clínica activa del paciente (debe haber solo una por paciente)
        List<MedicalRecord> existingRecords = medicalRecordRepository.findActiveByPatientId(patient.getId());
        MedicalRecord existingRecord = existingRecords != null && !existingRecords.isEmpty() ? existingRecords.get(0) : null;

        if (existingRecord != null) {
            // Si existe, actualizar agregando la información de la nueva cita
            log.info("Actualizando historia clínica existente ID: {} con cita ID: {}", existingRecord.getId(), appointmentId);
            
            // Actualizar veterinario si es diferente
            if (!existingRecord.getVeterinarian().getId().equals(veterinarian.getId())) {
                existingRecord.setVeterinarian(veterinarian);
            }
            
            // Actualizar fecha de registro a la más reciente
            if (appointment.getScheduledDate().isAfter(existingRecord.getRecordDate())) {
                existingRecord.setRecordDate(appointment.getScheduledDate());
            }
            
            // Agregar información de la nueva cita a las notas
            String appointmentNotes = String.format("\n\n[%s] Cita: %s - %s", 
                appointment.getScheduledDate().toString(),
                appointment.getAppointmentType(), 
                appointment.getReason() != null ? appointment.getReason() : "Sin motivo especificado");
            
            if (existingRecord.getNotes() != null && !existingRecord.getNotes().isEmpty()) {
                existingRecord.setNotes(existingRecord.getNotes() + appointmentNotes);
            } else {
                existingRecord.setNotes(appointmentNotes.trim());
            }

            MedicalRecord updatedRecord = medicalRecordRepository.save(existingRecord);
            log.info("Historia clínica actualizada exitosamente con ID: {}", updatedRecord.getId());
            return mapToDTO(updatedRecord);
        } else {
            // Si no existe, crear una nueva historia clínica (una por paciente)
            log.info("Creando nueva historia clínica para paciente ID: {} con cita ID: {}", patient.getId(), appointmentId);
            
            MedicalRecordBuilder builder = new MedicalRecordBuilder()
                .conPaciente(patient)
                .conVeterinario(veterinarian)
                .conCita(appointment)
                .conFechaRegistro(appointment.getScheduledDate())
                .conDiagnostico("Historia clínica iniciada - " + appointment.getAppointmentType())
                .conTratamiento("En espera de evaluación durante la cita")
                .conSintomas(appointment.getReason() != null ? appointment.getReason() : "Sin síntomas reportados")
                .conNotas(String.format("[%s] Cita agendada: %s - %s", 
                    appointment.getScheduledDate().toString(),
                    appointment.getAppointmentType(), 
                    appointment.getReason() != null ? appointment.getReason() : "Sin motivo especificado"))
                .activo(true);

            MedicalRecord newRecord = builder.build();
            MedicalRecord savedRecord = medicalRecordRepository.save(newRecord);
            log.info("Nueva historia clínica creada exitosamente con ID: {}", savedRecord.getId());
            return mapToDTO(savedRecord);
        }
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
        dto.setHeartRate(medicalRecord.getHeartRate());
        dto.setNotes(medicalRecord.getNotes());
        dto.setFollowUpRequired(medicalRecord.getFollowUpRequired());
        dto.setFollowUpDate(medicalRecord.getFollowUpDate());
        dto.setIsActive(medicalRecord.getIsActive());
        dto.setPrescriptionCount(medicalRecord.getPrescriptions() != null ? 
                                medicalRecord.getPrescriptions().size() : 0);
        dto.setInformedConsentCount(medicalRecord.getInformedConsents() != null ? 
                                   medicalRecord.getInformedConsents().size() : 0);
        dto.setCreatedAt(medicalRecord.getCreatedAt());
        dto.setUpdatedAt(medicalRecord.getUpdatedAt());
        
        return dto;
    }
}
