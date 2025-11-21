package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.service.CreateServiceRequest;
import com.vetclinic.dto.service.ServiceDTO;
import com.vetclinic.dto.service.UpdateServiceRequest;
import com.vetclinic.service.ClinicService;
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
 * Controlador REST para la gestión del catálogo de servicios
 * RF017 - Catálogo de Servicios
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClinicServiceController {

    private final ClinicService clinicService;

    /**
     * Crear un nuevo servicio
     * POST /api/services
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@Valid @RequestBody CreateServiceRequest request) {
        log.info("POST /api/services - Creando nuevo servicio");
        ServiceDTO service = clinicService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servicio creado exitosamente", service));
    }

    /**
     * Obtener servicios con paginación
     * GET /api/services/page?page=0&size=10
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<ServiceDTO>>> getServicesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("GET /api/services/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ServiceDTO> servicesPage = clinicService.getServicesPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de servicios obtenida exitosamente", servicesPage));
    }

    /**
     * Obtener todos los servicios activos
     * GET /api/services
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        log.info("GET /api/services - Obteniendo todos los servicios activos");
        List<ServiceDTO> services = clinicService.getAllActiveServices();
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos exitosamente", services));
    }

    /**
     * Obtener servicio por ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ServiceDTO>> getServiceById(@PathVariable Long id) {
        log.info("GET /api/services/{} - Obteniendo servicio", id);
        ServiceDTO service = clinicService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio obtenido exitosamente", service));
    }

    /**
     * Buscar servicios por nombre
     * GET /api/services/search?q=nombre&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<ServiceDTO>>> searchServices(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/services/search - q: {}", q);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ServiceDTO> results = clinicService.searchServices(q, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada exitosamente", results));
    }

    /**
     * Obtener servicios por categoría
     * GET /api/services/category/{category}?page=0&size=10
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<ServiceDTO>>> getServicesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/services/category/{} - Obteniendo servicios por categoría", category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ServiceDTO> services = clinicService.getServicesByCategory(category, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos exitosamente", services));
    }

    /**
     * Obtener todos los servicios activos por categoría (sin paginación)
     * GET /api/services/category/{category}/all
     */
    @GetMapping("/category/{category}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServicesByCategory(@PathVariable String category) {
        log.info("GET /api/services/category/{}/all - Obteniendo todos los servicios por categoría", category);
        List<ServiceDTO> services = clinicService.getAllServicesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos exitosamente", services));
    }

    /**
     * Actualizar servicio
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        
        log.info("PUT /api/services/{} - Actualizando servicio", id);
        ServiceDTO service = clinicService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado exitosamente", service));
    }

    /**
     * Eliminar servicio (soft delete)
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id) {
        log.info("DELETE /api/services/{} - Eliminando servicio", id);
        clinicService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio eliminado exitosamente", null));
    }

    /**
     * Contar servicios activos
     * GET /api/services/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countServices() {
        log.info("GET /api/services/count - Contando servicios activos");
        long count = clinicService.countActiveServices();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }
}



