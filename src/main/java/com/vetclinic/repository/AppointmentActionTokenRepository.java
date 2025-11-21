package com.vetclinic.repository;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.AppointmentActionToken;
import com.vetclinic.entity.AppointmentActionToken.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentActionTokenRepository extends JpaRepository<AppointmentActionToken, UUID> {

    Optional<AppointmentActionToken> findByToken(String token);

    Optional<AppointmentActionToken> findByAppointmentAndActionTypeAndUsedAtIsNull(
            Appointment appointment, 
            ActionType actionType
    );

    @Modifying
    @Query("DELETE FROM AppointmentActionToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AppointmentActionToken t WHERE t.appointment = :appointment")
    void deleteByAppointment(@Param("appointment") Appointment appointment);

    @Query("SELECT COUNT(t) > 0 FROM AppointmentActionToken t WHERE t.appointment.id = :appointmentId " +
           "AND t.actionType = :actionType AND t.usedAt IS NULL AND t.expiresAt > :now")
    boolean existsValidToken(@Param("appointmentId") Long appointmentId, 
                            @Param("actionType") ActionType actionType,
                            @Param("now") LocalDateTime now);
}

