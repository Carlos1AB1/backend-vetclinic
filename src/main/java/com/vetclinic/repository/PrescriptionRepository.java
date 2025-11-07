package com.vetclinic.repository;

import com.vetclinic.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Prescription
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Encontrar prescripciones activas paginadas
     */
    Page<Prescription> findByIsActiveTrueOrderByStartDateDesc(Pageable pageable);

    /**
     * Encontrar prescripciones por paciente
     */
    List<Prescription> findByPatientIdAndIsActiveTrueOrderByStartDateDesc(Long patientId);

    /**
     * Encontrar prescripciones por registro médico
     */
    List<Prescription> findByMedicalRecordIdAndIsActiveTrueOrderByStartDateDesc(Long medicalRecordId);

    /**
     * Encontrar prescripciones activas de un paciente (dentro del período)
     */
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
           "AND p.isActive = true " +
           "AND p.startDate <= CURRENT_TIMESTAMP " +
           "AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP) " +
           "ORDER BY p.startDate DESC")
    List<Prescription> findActivePrescriptionsByPatient(@Param("patientId") Long patientId);

    /**
     * Buscar prescripciones por medicamento
     */
    @Query("SELECT p FROM Prescription p WHERE LOWER(p.medicationName) LIKE LOWER(CONCAT('%', :medication, '%')) " +
           "AND p.isActive = true ORDER BY p.startDate DESC")
    Page<Prescription> searchByMedication(@Param("medication") String medication, Pageable pageable);

    /**
     * Encontrar prescripciones que expiran pronto (próximos 7 días)
     */
    @Query("SELECT p FROM Prescription p WHERE p.isActive = true " +
           "AND p.endDate BETWEEN CURRENT_TIMESTAMP AND :endDate " +
           "ORDER BY p.endDate ASC")
    List<Prescription> findExpiringPrescriptions(@Param("endDate") LocalDateTime endDate);

    /**
     * Encontrar prescripciones expiradas
     */
    @Query("SELECT p FROM Prescription p WHERE p.isActive = true " +
           "AND p.endDate < CURRENT_TIMESTAMP " +
           "ORDER BY p.endDate DESC")
    List<Prescription> findExpiredPrescriptions();

    /**
     * Contar prescripciones activas
     */
    long countByIsActiveTrue();

    /**
     * Contar prescripciones activas de un paciente
     */
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patient.id = :patientId " +
           "AND p.isActive = true " +
           "AND p.startDate <= CURRENT_TIMESTAMP " +
           "AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP)")
    long countActivePrescriptionsByPatient(@Param("patientId") Long patientId);
}
