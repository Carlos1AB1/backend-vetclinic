package com.vetclinic.service;

import com.vetclinic.dto.prescription.CreatePrescriptionRequest;
import com.vetclinic.dto.prescription.PrescriptionDTO;
import com.vetclinic.dto.prescription.UpdatePrescriptionRequest;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.Prescription;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.MedicalRecordRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de prescripciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    /**
     * Crear una nueva prescripción
     */
    public PrescriptionDTO createPrescription(CreatePrescriptionRequest request) {
        log.info("Creando nueva prescripción para paciente ID: {}", request.getPatientId());

        // Validar que el registro médico existe
        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
            .orElseThrow(() -> new ResourceNotFoundException("Registro médico no encontrado con ID: " + request.getMedicalRecordId()));

        // Validar que el paciente existe
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getPatientId()));

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(medicalRecord);
        prescription.setPatient(patient);
        prescription.setMedicationName(request.getMedicationName());
        prescription.setDosage(request.getDosage());
        prescription.setFrequency(request.getFrequency());
        prescription.setDuration(request.getDuration());
        prescription.setInstructions(request.getInstructions());
        prescription.setStartDate(request.getStartDate());
        prescription.setEndDate(request.getEndDate());
        prescription.setNotes(request.getNotes());
        prescription.setIsActive(true);

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescripción creada exitosamente con ID: {}", savedPrescription.getId());

        return mapToDTO(savedPrescription);
    }

    /**
     * Obtener prescripción por ID
     */
    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionById(Long id) {
        log.info("Buscando prescripción con ID: {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescripción no encontrada con ID: " + id));
        return mapToDTO(prescription);
    }

    /**
     * Obtener prescripciones con paginación
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDTO> getPrescriptionsPage(Pageable pageable) {
        log.info("Obteniendo página de prescripciones: {}", pageable);
        return prescriptionRepository.findByIsActiveTrueOrderByStartDateDesc(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener prescripciones por paciente
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
        log.info("Obteniendo prescripciones del paciente: {}", patientId);
        return prescriptionRepository.findByPatientIdAndIsActiveTrueOrderByStartDateDesc(patientId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener prescripciones activas por paciente
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getActivePrescriptionsByPatient(Long patientId) {
        log.info("Obteniendo prescripciones activas del paciente: {}", patientId);
        return prescriptionRepository.findActivePrescriptionsByPatient(patientId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener prescripciones por registro médico
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByMedicalRecord(Long medicalRecordId) {
        log.info("Obteniendo prescripciones del registro médico: {}", medicalRecordId);
        return prescriptionRepository.findByMedicalRecordIdAndIsActiveTrueOrderByStartDateDesc(medicalRecordId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Buscar prescripciones por medicamento
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDTO> searchByMedication(String medication, Pageable pageable) {
        log.info("Buscando prescripciones por medicamento: {}", medication);
        return prescriptionRepository.searchByMedication(medication, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener prescripciones que expiran pronto
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getExpiringPrescriptions() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        log.info("Obteniendo prescripciones que expiran pronto");
        return prescriptionRepository.findExpiringPrescriptions(endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar prescripción
     */
    public PrescriptionDTO updatePrescription(Long id, UpdatePrescriptionRequest request) {
        log.info("Actualizando prescripción con ID: {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescripción no encontrada con ID: " + id));

        // Actualizar campos
        if (request.getMedicationName() != null) prescription.setMedicationName(request.getMedicationName());
        if (request.getDosage() != null) prescription.setDosage(request.getDosage());
        if (request.getFrequency() != null) prescription.setFrequency(request.getFrequency());
        if (request.getDuration() != null) prescription.setDuration(request.getDuration());
        if (request.getInstructions() != null) prescription.setInstructions(request.getInstructions());
        if (request.getStartDate() != null) prescription.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) prescription.setEndDate(request.getEndDate());
        if (request.getNotes() != null) prescription.setNotes(request.getNotes());
        if (request.getIsActive() != null) prescription.setIsActive(request.getIsActive());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescripción actualizada exitosamente");

        return mapToDTO(updatedPrescription);
    }

    /**
     * Eliminar prescripción (soft delete)
     */
    public void deletePrescription(Long id) {
        log.info("Eliminando prescripción con ID: {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescripción no encontrada con ID: " + id));

        prescription.setIsActive(false);
        prescriptionRepository.save(prescription);

        log.info("Prescripción eliminada exitosamente");
    }

    /**
     * Contar prescripciones activas
     */
    @Transactional(readOnly = true)
    public long countActivePrescriptions() {
        return prescriptionRepository.countByIsActiveTrue();
    }

    /**
     * Mapear entidad a DTO
     */
    private PrescriptionDTO mapToDTO(Prescription prescription) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        
        // Información del registro médico
        if (prescription.getMedicalRecord() != null) {
            dto.setMedicalRecordId(prescription.getMedicalRecord().getId());
            dto.setMedicalRecordDate(prescription.getMedicalRecord().getRecordDate());
        }
        
        // Información del paciente
        if (prescription.getPatient() != null) {
            dto.setPatientId(prescription.getPatient().getId());
            dto.setPatientName(prescription.getPatient().getName());
        }
        
        dto.setMedicationName(prescription.getMedicationName());
        dto.setDosage(prescription.getDosage());
        dto.setFrequency(prescription.getFrequency());
        dto.setDuration(prescription.getDuration());
        dto.setInstructions(prescription.getInstructions());
        dto.setStartDate(prescription.getStartDate());
        dto.setEndDate(prescription.getEndDate());
        dto.setNotes(prescription.getNotes());
        dto.setIsActive(prescription.getIsActive());
        dto.setIsExpired(prescription.isExpired());
        dto.setIsCurrentlyActive(prescription.isCurrentlyActive());
        dto.setCreatedAt(prescription.getCreatedAt());
        dto.setUpdatedAt(prescription.getUpdatedAt());
        
        return dto;
    }
}
