package com.vetclinic.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para actualizar un item de inventario
 * RF019 - Gestión de Inventario
 */
@Data
public class UpdateInventoryItemRequest {

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String category;

    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    private String sku;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer quantity;

    @Min(value = 0, message = "El nivel mínimo de stock no puede ser negativo")
    private Integer minStockLevel;

    @Min(value = 0, message = "El nivel máximo de stock no puede ser negativo")
    private Integer maxStockLevel;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio unitario tiene formato inválido")
    private BigDecimal unitPrice;

    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    private String supplier;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String location;

    private Boolean isActive;
}

