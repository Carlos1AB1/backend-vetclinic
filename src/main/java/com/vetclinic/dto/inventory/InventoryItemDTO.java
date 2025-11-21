package com.vetclinic.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar un item de inventario
 * RF019 - Gestión de Inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String sku;
    private Integer quantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal unitPrice;
    private String supplier;
    private LocalDateTime expirationDate;
    private String location;
    private Boolean isActive;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean isExpiringSoon;
    private Boolean isExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



