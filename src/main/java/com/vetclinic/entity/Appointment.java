package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad para representar citas veterinarias
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private User veterinarian;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "appointment_type", nullable = false, length = 50)
    private String appointmentType; // CONSULTATION, VACCINATION, SURGERY, CHECKUP, EMERGENCY

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum para el estado de la cita
     */
    public enum AppointmentStatus {
        SCHEDULED,      // Programada
        CONFIRMED,      // Confirmada
        IN_PROGRESS,    // En progreso
        COMPLETED,      // Completada
        CANCELLED,      // Cancelada
        NO_SHOW         // No asistió
    }

    /**
     * Verificar si la cita está pendiente
     */
    public boolean isPending() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Verificar si la cita está finalizada
     */
    public boolean isFinished() {
        return status == AppointmentStatus.COMPLETED || 
               status == AppointmentStatus.CANCELLED || 
               status == AppointmentStatus.NO_SHOW;
    }

    /**
     * Verificar si la cita puede ser cancelada
     */
    public boolean canBeCancelled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }
}
