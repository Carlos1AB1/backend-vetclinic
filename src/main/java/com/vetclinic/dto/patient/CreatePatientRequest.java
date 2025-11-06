package com.vetclinic.dto.patient;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la creación de pacientes
 * Implementa patrón DTO (Data Transfer Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientRequest {

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "La especie es obligatoria")
    @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
    private String species; // Perro, Gato, Ave, Reptil, etc.

    @Size(max = 50, message = "La raza no puede exceder 50 caracteres")
    private String breed;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthDate;

    @NotNull(message = "El género es obligatorio")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "El género debe ser: MALE o FEMALE")
    private String gender;

    @Size(max = 20, message = "El color no puede exceder 20 caracteres")
    private String color;

    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    @DecimalMax(value = "999.99", message = "El peso no puede exceder 999.99 kg")
    private BigDecimal weight;

    @Size(max = 50, message = "El número de microchip no puede exceder 50 caracteres")
    private String microchipNumber;

    @Size(max = 500, message = "Las alergias no pueden exceder 500 caracteres")
    private String allergies;

    @Size(max = 1000, message = "El historial médico no puede exceder 1000 caracteres")
    private String medicalHistory;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    @NotNull(message = "El ID del propietario es obligatorio")
    private Long ownerId;
}
