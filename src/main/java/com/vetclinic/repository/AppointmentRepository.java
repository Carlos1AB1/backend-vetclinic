package com.vetclinic.repository;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
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
 * Repositorio para la entidad Appointment
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Encontrar citas activas paginadas
     */
    Page<Appointment> findByIsActiveTrueOrderByScheduledDateDesc(Pageable pageable);

    /**
     * Encontrar citas por paciente
     */
    List<Appointment> findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(Long patientId);
    List<Appointment> findByOwnerIdOrderByScheduledDateDesc(Long ownerId);

    /**
     * Encontrar citas por propietario
     */
    List<Appointment> findByOwnerIdAndIsActiveTrueOrderByScheduledDateDesc(Long ownerId);

    /**
     * Encontrar citas por veterinario
     */
    List<Appointment> findByVeterinarianIdAndIsActiveTrueOrderByScheduledDateDesc(UUID veterinarianId);
    
    /**
     * Encontrar todas las citas por veterinario (activas e inactivas)
     */
    List<Appointment> findByVeterinarianIdOrderByScheduledDateDesc(UUID veterinarianId);

    /**
     * Encontrar citas por estado
     */
    Page<Appointment> findByStatusAndIsActiveTrueOrderByScheduledDateDesc(AppointmentStatus status, Pageable pageable);

    /**
     * Encontrar citas por rango de fechas
     */
    @Query("SELECT a FROM Appointment a WHERE a.scheduledDate BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.scheduledDate ASC")
    List<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Encontrar citas por rango de fechas con relaciones cargadas (para reportes)
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.patient " +
           "LEFT JOIN FETCH a.owner " +
           "LEFT JOIN FETCH a.veterinarian " +
           "WHERE a.scheduledDate BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.scheduledDate ASC")
    List<Appointment> findByDateRangeWithRelations(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Encontrar citas de un veterinario en un rango de fechas
     */
    @Query("SELECT a FROM Appointment a WHERE a.veterinarian.id = :veterinarianId " +
           "AND a.scheduledDate BETWEEN :startDate AND :endDate " +
           "AND a.isActive = true ORDER BY a.scheduledDate ASC")
    List<Appointment> findByVeterinarianAndDateRange(@Param("veterinarianId") UUID veterinarianId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Verificar conflicto de horario para un veterinario
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.veterinarian.id = :veterinarianId " +
           "AND a.scheduledDate = :scheduledDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "AND a.isActive = true")
    boolean existsConflict(@Param("veterinarianId") UUID veterinarianId,
                          @Param("scheduledDate") LocalDateTime scheduledDate);

    /**
     * Verificar conflicto excluyendo una cita específica (para updates)
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.veterinarian.id = :veterinarianId " +
           "AND a.scheduledDate = :scheduledDate " +
           "AND a.id != :appointmentId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "AND a.isActive = true")
    boolean existsConflictExcluding(@Param("veterinarianId") UUID veterinarianId,
                                   @Param("scheduledDate") LocalDateTime scheduledDate,
                                   @Param("appointmentId") Long appointmentId);

    /**
     * Contar citas activas
     */
    long countByIsActiveTrue();

    /**
     * Contar citas por estado
     */
    long countByStatusAndIsActiveTrue(AppointmentStatus status);

    /**
     * Contar citas por propietario
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * Buscar citas próximas (siguientes 7 días)
     */
    @Query("SELECT a FROM Appointment a WHERE a.scheduledDate BETWEEN :now AND :endDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.isActive = true ORDER BY a.scheduledDate ASC")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar citas de hoy para un veterinario
     */
    @Query("SELECT a FROM Appointment a WHERE a.veterinarian.id = :veterinarianId " +
           "AND a.scheduledDate >= :startOfDay AND a.scheduledDate < :endOfDay " +
           "AND a.isActive = true ORDER BY a.scheduledDate ASC")
    List<Appointment> findTodayAppointments(@Param("veterinarianId") UUID veterinarianId,
                                          @Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("endOfDay") LocalDateTime endOfDay);
}
