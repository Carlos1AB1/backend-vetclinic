package com.vetclinic.repository;

import com.vetclinic.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad InventoryItem
 * RF019 - Gestión de Inventario
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Encontrar items activos paginados
     */
    Page<InventoryItem> findByIsActiveTrueOrderByNameAsc(Pageable pageable);

    /**
     * Encontrar items por categoría
     */
    Page<InventoryItem> findByCategoryAndIsActiveTrueOrderByNameAsc(String category, Pageable pageable);

    /**
     * Encontrar items con stock bajo
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minStockLevel AND i.isActive = true ORDER BY i.quantity ASC")
    List<InventoryItem> findLowStockItems();

    /**
     * Encontrar items agotados
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= 0 AND i.isActive = true ORDER BY i.name ASC")
    List<InventoryItem> findOutOfStockItems();

    /**
     * Encontrar items próximos a vencer
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.expirationDate BETWEEN :now AND :expirationDate " +
           "AND i.isActive = true ORDER BY i.expirationDate ASC")
    List<InventoryItem> findExpiringSoonItems(@Param("now") LocalDateTime now, 
                                             @Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Encontrar items vencidos
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.expirationDate < :now AND i.isActive = true ORDER BY i.expirationDate ASC")
    List<InventoryItem> findExpiredItems(@Param("now") LocalDateTime now);

    /**
     * Buscar items por nombre o SKU
     */
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND i.isActive = true ORDER BY i.name ASC")
    Page<InventoryItem> searchItems(@Param("search") String search, Pageable pageable);

    /**
     * Buscar por SKU exacto
     */
    Optional<InventoryItem> findBySkuAndIsActiveTrue(String sku);

    /**
     * Contar items activos
     */
    long countByIsActiveTrue();

    /**
     * Contar items con stock bajo
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.quantity <= i.minStockLevel AND i.isActive = true")
    long countLowStockItems();
}



