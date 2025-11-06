package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un paciente (mascota) en el sistema
 * Implementa patrón Entity de JPA
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String species; // Perro, Gato, Ave, etc.

    @Column(length = 50)
    private String breed; // Raza

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String gender; // MALE, FEMALE

    @Column(length = 20)
    private String color;

    @Column(precision = 5, scale = 2)
    private Double weight; // en kg

    @Column(name = "microchip_number", length = 50, unique = true)
    private String microchipNumber;

    @Column(length = 500)
    private String allergies; // Alergias conocidas

    @Column(length = 1000)
    private String medicalHistory; // Historial médico resumido

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String notes; // Notas adicionales

    // Relación con el propietario (Owner) - se implementará en el módulo de owners
    @Column(name = "owner_id")
    private Long ownerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calcula la edad del paciente en años
     */
    @Transient
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}
