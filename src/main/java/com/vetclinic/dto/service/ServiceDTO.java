package com.vetclinic.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar un servicio
 * RF017 - Catálogo de Servicios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer durationMinutes;
    private Boolean requiresAppointment;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



