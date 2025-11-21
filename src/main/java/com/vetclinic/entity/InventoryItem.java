package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para representar items del inventario
 * RF019 - Gestión de Inventario
 */
@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category; // MEDICATION, SUPPLY, EQUIPMENT, FOOD, etc.

    @Column(name = "sku", unique = true, length = 50)
    private String sku; // Stock Keeping Unit

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "location", length = 100)
    private String location; // Ubicación física en el almacén

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Verificar si el stock está bajo
     */
    public boolean isLowStock() {
        return quantity <= minStockLevel;
    }

    /**
     * Verificar si el stock está agotado
     */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    /**
     * Verificar si el item está próximo a vencer
     */
    public boolean isExpiringSoon() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDateTime.now().plusDays(30)) 
            && expirationDate.isAfter(LocalDateTime.now());
    }

    /**
     * Verificar si el item está vencido
     */
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDateTime.now());
    }
}



