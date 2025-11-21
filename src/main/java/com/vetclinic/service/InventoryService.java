package com.vetclinic.service;

import com.vetclinic.dto.inventory.CreateInventoryItemRequest;
import com.vetclinic.dto.inventory.InventoryItemDTO;
import com.vetclinic.dto.inventory.UpdateInventoryItemRequest;
import com.vetclinic.entity.InventoryItem;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.DuplicateResourceException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de inventario
 * RF019 - Gestión de Inventario
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Crear un nuevo item de inventario
     */
    public InventoryItemDTO createInventoryItem(CreateInventoryItemRequest request) {
        log.info("Creando nuevo item de inventario: {}", request.getName());

        // Validar SKU único si se proporciona
        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (inventoryItemRepository.findBySkuAndIsActiveTrue(request.getSku()).isPresent()) {
                throw new DuplicateResourceException("Ya existe un item con el SKU: " + request.getSku());
            }
        }

        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setSku(request.getSku());
        item.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0);
        item.setMinStockLevel(request.getMinStockLevel() != null ? request.getMinStockLevel() : 10);
        item.setMaxStockLevel(request.getMaxStockLevel());
        item.setUnitPrice(request.getUnitPrice());
        item.setSupplier(request.getSupplier());
        // Convertir LocalDate a LocalDateTime (inicio del día)
        if (request.getExpirationDate() != null) {
            item.setExpirationDate(request.getExpirationDate().atStartOfDay());
        } else {
            item.setExpirationDate(null);
        }
        item.setLocation(request.getLocation());
        item.setIsActive(true);

        InventoryItem savedItem = inventoryItemRepository.save(item);
        log.info("Item de inventario creado exitosamente con ID: {}", savedItem.getId());

        return mapToDTO(savedItem);
    }

    /**
     * Obtener item por ID
     */
    @Transactional(readOnly = true)
    public InventoryItemDTO getInventoryItemById(Long id) {
        log.info("Buscando item de inventario con ID: {}", id);
        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item de inventario no encontrado con ID: " + id));
        return mapToDTO(item);
    }

    /**
     * Obtener items con paginación
     */
    @Transactional(readOnly = true)
    public Page<InventoryItemDTO> getInventoryItemsPage(Pageable pageable) {
        log.info("Obteniendo página de items de inventario: {}", pageable);
        return inventoryItemRepository.findByIsActiveTrueOrderByNameAsc(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener items por categoría
     */
    @Transactional(readOnly = true)
    public Page<InventoryItemDTO> getInventoryItemsByCategory(String category, Pageable pageable) {
        log.info("Obteniendo items de inventario por categoría: {}", category);
        return inventoryItemRepository.findByCategoryAndIsActiveTrueOrderByNameAsc(category, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Buscar items por nombre o SKU
     */
    @Transactional(readOnly = true)
    public Page<InventoryItemDTO> searchInventoryItems(String search, Pageable pageable) {
        log.info("Buscando items de inventario: {}", search);
        return inventoryItemRepository.searchItems(search, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener items con stock bajo
     */
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getLowStockItems() {
        log.info("Obteniendo items con stock bajo");
        return inventoryItemRepository.findLowStockItems()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener items agotados
     */
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getOutOfStockItems() {
        log.info("Obteniendo items agotados");
        return inventoryItemRepository.findOutOfStockItems()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener items próximos a vencer
     */
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getExpiringSoonItems() {
        log.info("Obteniendo items próximos a vencer");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(30);
        return inventoryItemRepository.findExpiringSoonItems(now, expirationDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener items vencidos
     */
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getExpiredItems() {
        log.info("Obteniendo items vencidos");
        return inventoryItemRepository.findExpiredItems(LocalDateTime.now())
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar item de inventario
     */
    public InventoryItemDTO updateInventoryItem(Long id, UpdateInventoryItemRequest request) {
        log.info("Actualizando item de inventario con ID: {}", id);

        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item de inventario no encontrado con ID: " + id));

        // Validar SKU único si cambió
        if (request.getSku() != null && !request.getSku().equals(item.getSku())) {
            if (inventoryItemRepository.findBySkuAndIsActiveTrue(request.getSku()).isPresent()) {
                throw new DuplicateResourceException("Ya existe un item con el SKU: " + request.getSku());
            }
            item.setSku(request.getSku());
        }

        // Actualizar campos
        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getMinStockLevel() != null) item.setMinStockLevel(request.getMinStockLevel());
        if (request.getMaxStockLevel() != null) item.setMaxStockLevel(request.getMaxStockLevel());
        if (request.getUnitPrice() != null) item.setUnitPrice(request.getUnitPrice());
        if (request.getSupplier() != null) item.setSupplier(request.getSupplier());
        // Convertir LocalDate a LocalDateTime (inicio del día)
        if (request.getExpirationDate() != null) {
            item.setExpirationDate(request.getExpirationDate().atStartOfDay());
        }
        if (request.getLocation() != null) item.setLocation(request.getLocation());
        if (request.getIsActive() != null) item.setIsActive(request.getIsActive());

        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Item de inventario actualizado exitosamente");

        return mapToDTO(updatedItem);
    }

    /**
     * Actualizar cantidad de stock
     */
    public InventoryItemDTO updateStock(Long id, Integer quantityChange) {
        log.info("Actualizando stock del item ID: {} con cambio: {}", id, quantityChange);

        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item de inventario no encontrado con ID: " + id));

        int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new BusinessException("No se puede reducir el stock por debajo de 0");
        }

        item.setQuantity(newQuantity);
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Stock actualizado exitosamente. Nueva cantidad: {}", newQuantity);

        return mapToDTO(updatedItem);
    }

    /**
     * Eliminar item (soft delete)
     */
    public void deleteInventoryItem(Long id) {
        log.info("Eliminando item de inventario con ID: {}", id);

        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item de inventario no encontrado con ID: " + id));

        item.setIsActive(false);
        inventoryItemRepository.save(item);

        log.info("Item de inventario eliminado exitosamente");
    }

    /**
     * Contar items activos
     */
    @Transactional(readOnly = true)
    public long countActiveInventoryItems() {
        return inventoryItemRepository.countByIsActiveTrue();
    }

    /**
     * Contar items con stock bajo
     */
    @Transactional(readOnly = true)
    public long countLowStockItems() {
        return inventoryItemRepository.countLowStockItems();
    }

    /**
     * Verificar disponibilidad de un item
     */
    @Transactional(readOnly = true)
    public boolean checkAvailability(String itemName, int quantity) {
        InventoryItem item = inventoryItemRepository.searchItems(itemName, Pageable.unpaged())
            .getContent()
            .stream()
            .findFirst()
            .orElse(null);

        if (item == null) {
            return false;
        }

        return item.getQuantity() >= quantity;
    }

    /**
     * Mapear entidad a DTO
     */
    private InventoryItemDTO mapToDTO(InventoryItem item) {
        return InventoryItemDTO.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .category(item.getCategory())
            .sku(item.getSku())
            .quantity(item.getQuantity())
            .minStockLevel(item.getMinStockLevel())
            .maxStockLevel(item.getMaxStockLevel())
            .unitPrice(item.getUnitPrice())
            .supplier(item.getSupplier())
            .expirationDate(item.getExpirationDate())
            .location(item.getLocation())
            .isActive(item.getIsActive())
            .isLowStock(item.isLowStock())
            .isOutOfStock(item.isOutOfStock())
            .isExpiringSoon(item.isExpiringSoon())
            .isExpired(item.isExpired())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}

