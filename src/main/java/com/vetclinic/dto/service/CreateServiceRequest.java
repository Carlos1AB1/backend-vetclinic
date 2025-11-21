package com.vetclinic.dto.service;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para crear un servicio
 * RF017 - Catálogo de Servicios
 */
@Data
public class CreateServiceRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotBlank(message = "La categoría es requerida")
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String category;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio tiene formato inválido")
    private BigDecimal price;

    @NotNull(message = "La duración es requerida")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer durationMinutes;

    @NotNull(message = "El campo requiresAppointment es requerido")
    private Boolean requiresAppointment;
}



