package com.vetclinic.repository;

import com.vetclinic.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad MedicalRecord
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /**
     * Encontrar registros médicos activos paginados
     */
    Page<MedicalRecord> findByIsActiveTrueOrderByRecordDateDesc(Pageable pageable);

    /**
     * Encontrar registros médicos por paciente
     */
    List<MedicalRecord> findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(Long patientId);

    /**
     * Encontrar registros médicos por cita
     */
    List<MedicalRecord> findByAppointmentIdAndIsActiveTrueOrderByRecordDateDesc(Long appointmentId);

    /**
     * Encontrar registros médicos por veterinario
     */
    List<MedicalRecord> findByVeterinarianIdAndIsActiveTrueOrderByRecordDateDesc(UUID veterinarianId);

    /**
     * Encontrar registros médicos por rango de fechas
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.recordDate BETWEEN :startDate AND :endDate " +
           "AND mr.isActive = true ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar registros por diagnóstico
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :diagnosis, '%')) " +
           "AND mr.isActive = true ORDER BY mr.recordDate DESC")
    Page<MedicalRecord> searchByDiagnosis(@Param("diagnosis") String diagnosis, Pageable pageable);

    /**
     * Encontrar registros que requieren seguimiento
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.followUpRequired = true " +
           "AND mr.followUpDate > CURRENT_TIMESTAMP " +
           "AND mr.isActive = true ORDER BY mr.followUpDate ASC")
    List<MedicalRecord> findRecordsRequiringFollowUp();

    /**
     * Encontrar registros de seguimiento vencidos
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.followUpRequired = true " +
           "AND mr.followUpDate < CURRENT_TIMESTAMP " +
           "AND mr.isActive = true ORDER BY mr.followUpDate ASC")
    List<MedicalRecord> findOverdueFollowUps();

    /**
     * Contar registros médicos activos
     */
    long countByIsActiveTrue();

    /**
     * Obtener último registro médico de un paciente
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId " +
           "AND mr.isActive = true ORDER BY mr.recordDate DESC LIMIT 1")
    MedicalRecord findLatestByPatientId(@Param("patientId") Long patientId);
}
