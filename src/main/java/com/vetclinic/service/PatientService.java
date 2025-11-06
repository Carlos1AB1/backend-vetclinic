package com.vetclinic.service;

import com.vetclinic.dto.patient.CreatePatientRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.patient.UpdatePatientRequest;
import com.vetclinic.entity.Patient;
import com.vetclinic.exception.DuplicateResourceException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de pacientes
 * Implementa patrón Service Layer y Facade
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Crear un nuevo paciente
     */
    public PatientDTO createPatient(CreatePatientRequest request) {
        log.info("Creando nuevo paciente: {}", request.getName());

        // Validar microchip único si se proporciona
        if (request.getMicrochipNumber() != null && !request.getMicrochipNumber().isBlank()) {
            if (patientRepository.existsByMicrochipNumber(request.getMicrochipNumber())) {
                throw new DuplicateResourceException(
                    "Ya existe un paciente con el microchip: " + request.getMicrochipNumber()
                );
            }
        }

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setSpecies(request.getSpecies());
        patient.setBreed(request.getBreed());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setColor(request.getColor());
        patient.setWeight(request.getWeight());
        patient.setMicrochipNumber(request.getMicrochipNumber());
        patient.setAllergies(request.getAllergies());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setNotes(request.getNotes());
        patient.setOwnerId(request.getOwnerId());
        patient.setIsActive(true);

        Patient savedPatient = patientRepository.save(patient);
        log.info("Paciente creado exitosamente con ID: {}", savedPatient.getId());

        return mapToDTO(savedPatient);
    }

    /**
     * Obtener paciente por ID
     */
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        log.info("Buscando paciente con ID: {}", id);
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));
        return mapToDTO(patient);
    }

    /**
     * Listar todos los pacientes activos
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        log.info("Obteniendo lista de todos los pacientes activos");
        return patientRepository.findByIsActiveTrue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Listar pacientes con paginación
     */
    @Transactional(readOnly = true)
    public Page<PatientDTO> getPatientsPage(Pageable pageable) {
        log.info("Obteniendo página de pacientes: {}", pageable);
        return patientRepository.findByIsActiveTrue(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Actualizar paciente
     */
    public PatientDTO updatePatient(Long id, UpdatePatientRequest request) {
        log.info("Actualizando paciente con ID: {}", id);

        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        // Validar microchip único si se actualiza
        if (request.getMicrochipNumber() != null && !request.getMicrochipNumber().isBlank()) {
            if (!request.getMicrochipNumber().equals(patient.getMicrochipNumber()) &&
                patientRepository.existsByMicrochipNumber(request.getMicrochipNumber())) {
                throw new DuplicateResourceException(
                    "Ya existe un paciente con el microchip: " + request.getMicrochipNumber()
                );
            }
        }

        // Actualizar solo los campos proporcionados
        if (request.getName() != null) patient.setName(request.getName());
        if (request.getSpecies() != null) patient.setSpecies(request.getSpecies());
        if (request.getBreed() != null) patient.setBreed(request.getBreed());
        if (request.getDateOfBirth() != null) patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getColor() != null) patient.setColor(request.getColor());
        if (request.getWeight() != null) patient.setWeight(request.getWeight());
        if (request.getMicrochipNumber() != null) patient.setMicrochipNumber(request.getMicrochipNumber());
        if (request.getAllergies() != null) patient.setAllergies(request.getAllergies());
        if (request.getMedicalHistory() != null) patient.setMedicalHistory(request.getMedicalHistory());
        if (request.getIsActive() != null) patient.setIsActive(request.getIsActive());
        if (request.getNotes() != null) patient.setNotes(request.getNotes());
        if (request.getOwnerId() != null) patient.setOwnerId(request.getOwnerId());

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Paciente actualizado exitosamente: {}", updatedPatient.getId());

        return mapToDTO(updatedPatient);
    }

    /**
     * Eliminar paciente (soft delete)
     */
    public void deletePatient(Long id) {
        log.info("Eliminando paciente con ID: {}", id);

        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        patient.setIsActive(false);
        patientRepository.save(patient);

        log.info("Paciente desactivado exitosamente: {}", id);
    }

    /**
     * Buscar pacientes por nombre
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatientsByName(String name) {
        log.info("Buscando pacientes por nombre: {}", name);
        return patientRepository.searchByName(name).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Buscar pacientes por propietario
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByOwner(Long ownerId) {
        log.info("Obteniendo pacientes del propietario: {}", ownerId);
        return patientRepository.findByOwnerId(ownerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Buscar pacientes por especie
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsBySpecies(String species) {
        log.info("Obteniendo pacientes de especie: {}", species);
        return patientRepository.findBySpecies(species).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Contar pacientes activos
     */
    @Transactional(readOnly = true)
    public long countActivePatients() {
        return patientRepository.countByIsActiveTrue();
    }

    /**
     * Mapear entidad a DTO
     */
    private PatientDTO mapToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setSpecies(patient.getSpecies());
        dto.setBreed(patient.getBreed());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setAge(patient.getAge()); // Calcula la edad
        dto.setGender(patient.getGender());
        dto.setColor(patient.getColor());
        dto.setWeight(patient.getWeight());
        dto.setMicrochipNumber(patient.getMicrochipNumber());
        dto.setAllergies(patient.getAllergies());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setIsActive(patient.getIsActive());
        dto.setNotes(patient.getNotes());
        dto.setOwnerId(patient.getOwnerId());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        return dto;
    }
}
