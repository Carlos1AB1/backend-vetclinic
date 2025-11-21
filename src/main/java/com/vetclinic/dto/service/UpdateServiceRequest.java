package com.vetclinic.dto.service;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para actualizar un servicio
 * RF017 - Catálogo de Servicios
 */
@Data
public class UpdateServiceRequest {

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String category;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio tiene formato inválido")
    private BigDecimal price;

    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer durationMinutes;

    private Boolean requiresAppointment;

    private Boolean isActive;
}



