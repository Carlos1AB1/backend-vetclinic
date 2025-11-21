package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.inventory.CreateInventoryItemRequest;
import com.vetclinic.dto.inventory.InventoryItemDTO;
import com.vetclinic.dto.inventory.UpdateInventoryItemRequest;
import com.vetclinic.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de inventario
 * RF019 - Gestión de Inventario
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Crear un nuevo item de inventario
     * POST /api/inventory
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request) {
        log.info("POST /api/inventory - Creando nuevo item de inventario");
        InventoryItemDTO item = inventoryService.createInventoryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item de inventario creado exitosamente", item));
    }

    /**
     * Obtener items con paginación
     * GET /api/inventory/page?page=0&size=10
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<InventoryItemDTO>>> getInventoryItemsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("GET /api/inventory/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<InventoryItemDTO> itemsPage = inventoryService.getInventoryItemsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de items obtenida exitosamente", itemsPage));
    }

    /**
     * Obtener item por ID
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> getInventoryItemById(@PathVariable Long id) {
        log.info("GET /api/inventory/{} - Obteniendo item de inventario", id);
        InventoryItemDTO item = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item obtenido exitosamente", item));
    }

    /**
     * Buscar items por nombre o SKU
     * GET /api/inventory/search?q=nombre&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<InventoryItemDTO>>> searchInventoryItems(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/inventory/search - q: {}", q);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<InventoryItemDTO> results = inventoryService.searchInventoryItems(q, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada exitosamente", results));
    }

    /**
     * Obtener items por categoría
     * GET /api/inventory/category/{category}?page=0&size=10
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<InventoryItemDTO>>> getInventoryItemsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/inventory/category/{} - Obteniendo items por categoría", category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<InventoryItemDTO> items = inventoryService.getInventoryItemsByCategory(category, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Items obtenidos exitosamente", items));
    }

    /**
     * Obtener items con stock bajo
     * GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getLowStockItems() {
        log.info("GET /api/inventory/low-stock - Obteniendo items con stock bajo");
        List<InventoryItemDTO> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success("Items con stock bajo obtenidos exitosamente", items));
    }

    /**
     * Obtener items agotados
     * GET /api/inventory/out-of-stock
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getOutOfStockItems() {
        log.info("GET /api/inventory/out-of-stock - Obteniendo items agotados");
        List<InventoryItemDTO> items = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.success("Items agotados obtenidos exitosamente", items));
    }

    /**
     * Obtener items próximos a vencer
     * GET /api/inventory/expiring-soon
     */
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getExpiringSoonItems() {
        log.info("GET /api/inventory/expiring-soon - Obteniendo items próximos a vencer");
        List<InventoryItemDTO> items = inventoryService.getExpiringSoonItems();
        return ResponseEntity.ok(ApiResponse.success("Items próximos a vencer obtenidos exitosamente", items));
    }

    /**
     * Obtener items vencidos
     * GET /api/inventory/expired
     */
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getExpiredItems() {
        log.info("GET /api/inventory/expired - Obteniendo items vencidos");
        List<InventoryItemDTO> items = inventoryService.getExpiredItems();
        return ResponseEntity.ok(ApiResponse.success("Items vencidos obtenidos exitosamente", items));
    }

    /**
     * Actualizar item de inventario
     * PUT /api/inventory/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryItemRequest request) {
        
        log.info("PUT /api/inventory/{} - Actualizando item de inventario", id);
        InventoryItemDTO item = inventoryService.updateInventoryItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item actualizado exitosamente", item));
    }

    /**
     * Actualizar stock de un item
     * PUT /api/inventory/{id}/stock?quantityChange=10
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantityChange) {
        
        log.info("PUT /api/inventory/{}/stock - Cambio: {}", id, quantityChange);
        InventoryItemDTO item = inventoryService.updateStock(id, quantityChange);
        return ResponseEntity.ok(ApiResponse.success("Stock actualizado exitosamente", item));
    }

    /**
     * Eliminar item (soft delete)
     * DELETE /api/inventory/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteInventoryItem(@PathVariable Long id) {
        log.info("DELETE /api/inventory/{} - Eliminando item de inventario", id);
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item eliminado exitosamente", null));
    }

    /**
     * Contar items activos
     * GET /api/inventory/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countInventoryItems() {
        log.info("GET /api/inventory/count - Contando items activos");
        long count = inventoryService.countActiveInventoryItems();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }

    /**
     * Contar items con stock bajo
     * GET /api/inventory/low-stock/count
     */
    @GetMapping("/low-stock/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countLowStockItems() {
        log.info("GET /api/inventory/low-stock/count - Contando items con stock bajo");
        long count = inventoryService.countLowStockItems();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }
}



