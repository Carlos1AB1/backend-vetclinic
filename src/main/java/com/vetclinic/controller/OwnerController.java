package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.owner.CreateOwnerRequest;
import com.vetclinic.dto.owner.OwnerDTO;
import com.vetclinic.dto.owner.UpdateOwnerRequest;
import com.vetclinic.service.OwnerService;
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
 * Controlador REST para la gestión de propietarios
 */
@RestController
@RequestMapping("/owners")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OwnerController {

    private final OwnerService ownerService;

    /**
     * Crear un nuevo propietario
     * POST /api/owners
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OwnerDTO>> createOwner(@Valid @RequestBody CreateOwnerRequest request) {
        log.info("POST /api/owners - Creando nuevo propietario");
        OwnerDTO owner = ownerService.createOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Propietario creado exitosamente", owner));
    }

    /**
     * Obtener todos los propietarios activos (sin paginación)
     * GET /api/owners
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<OwnerDTO>>> getAllOwners() {
        log.info("GET /api/owners - Obteniendo todos los propietarios");
        List<OwnerDTO> owners = ownerService.getAllOwners();
        return ResponseEntity.ok(ApiResponse.success("Propietarios obtenidos exitosamente", owners));
    }

    /**
     * Obtener propietarios con paginación
     * GET /api/owners/page?page=0&size=10&sortBy=lastName&sortDirection=ASC
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<OwnerDTO>>> getOwnersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("GET /api/owners/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OwnerDTO> ownersPage = ownerService.getOwnersPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de propietarios obtenida exitosamente", ownersPage));
    }

    /**
     * Obtener propietario por ID
     * GET /api/owners/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OwnerDTO>> getOwnerById(@PathVariable Long id) {
        log.info("GET /api/owners/{} - Obteniendo propietario", id);
        OwnerDTO owner = ownerService.getOwnerById(id);
        return ResponseEntity.ok(ApiResponse.success("Propietario obtenido exitosamente", owner));
    }

    /**
     * Buscar propietarios
     * GET /api/owners/search?query=juan&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<OwnerDTO>>> searchOwners(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/owners/search - query: {}", query);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        Page<OwnerDTO> results = ownerService.searchOwners(query, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada exitosamente", results));
    }

    /**
     * Actualizar propietario
     * PUT /api/owners/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OwnerDTO>> updateOwner(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOwnerRequest request) {
        
        log.info("PUT /api/owners/{} - Actualizando propietario", id);
        OwnerDTO owner = ownerService.updateOwner(id, request);
        return ResponseEntity.ok(ApiResponse.success("Propietario actualizado exitosamente", owner));
    }

    /**
     * Eliminar propietario (soft delete)
     * DELETE /api/owners/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteOwner(@PathVariable Long id) {
        log.info("DELETE /api/owners/{} - Eliminando propietario", id);
        ownerService.deleteOwner(id);
        return ResponseEntity.ok(ApiResponse.success("Propietario eliminado exitosamente", null));
    }

    /**
     * Contar propietarios activos
     * GET /api/owners/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countOwners() {
        log.info("GET /api/owners/count - Contando propietarios activos");
        long count = ownerService.countActiveOwners();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }

    /**
     * Obtener propietarios por ciudad
     * GET /api/owners/city/{city}
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<OwnerDTO>>> getOwnersByCity(@PathVariable String city) {
        log.info("GET /api/owners/city/{} - Obteniendo propietarios por ciudad", city);
        List<OwnerDTO> owners = ownerService.getOwnersByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Propietarios obtenidos exitosamente", owners));
    }
}
