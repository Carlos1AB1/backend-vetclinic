package com.vetclinic.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de pacientes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private Long id;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private Integer age; // Calculado
    private String gender; // MALE, FEMALE
    private String color;
    private BigDecimal weight;
    private String microchipNumber;
    private String allergies;
    private String medicalHistory;
    private Boolean isActive;
    private String notes;
    private Long ownerId;
    private String ownerName; // Se puede poblar cuando se integre el módulo de owners
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
