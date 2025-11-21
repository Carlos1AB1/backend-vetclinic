package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.consent.CreateInformedConsentRequest;
import com.vetclinic.dto.consent.InformedConsentDTO;
import com.vetclinic.service.InformedConsentService;
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
 * Controlador REST para la gestión de consentimientos informados
 * RF016 - Gestión de Consentimientos Informados
 */
@RestController
@RequestMapping("/informed-consents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InformedConsentController {

    private final InformedConsentService informedConsentService;

    /**
     * Crear un nuevo consentimiento informado
     * POST /api/informed-consents
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<InformedConsentDTO>> createInformedConsent(
            @Valid @RequestBody CreateInformedConsentRequest request) {
        log.info("POST /api/informed-consents - Creando nuevo consentimiento informado");
        InformedConsentDTO consent = informedConsentService.createInformedConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consentimiento informado creado exitosamente", consent));
    }

    /**
     * Obtener consentimientos con paginación
     * GET /api/informed-consents/page?page=0&size=10
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<InformedConsentDTO>>> getInformedConsentsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /api/informed-consents/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<InformedConsentDTO> consentsPage = informedConsentService.getInformedConsentsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de consentimientos obtenida exitosamente", consentsPage));
    }

    /**
     * Obtener consentimiento por ID
     * GET /api/informed-consents/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InformedConsentDTO>> getInformedConsentById(@PathVariable Long id) {
        log.info("GET /api/informed-consents/{} - Obteniendo consentimiento", id);
        InformedConsentDTO consent = informedConsentService.getInformedConsentById(id);
        return ResponseEntity.ok(ApiResponse.success("Consentimiento obtenido exitosamente", consent));
    }

    /**
     * Obtener consentimientos por paciente
     * GET /api/informed-consents/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InformedConsentDTO>>> getInformedConsentsByPatient(@PathVariable Long patientId) {
        log.info("GET /api/informed-consents/patient/{} - Obteniendo consentimientos del paciente", patientId);
        List<InformedConsentDTO> consents = informedConsentService.getInformedConsentsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Consentimientos obtenidos exitosamente", consents));
    }

    /**
     * Obtener consentimientos pendientes de firma
     * GET /api/informed-consents/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InformedConsentDTO>>> getPendingConsents() {
        log.info("GET /api/informed-consents/pending - Obteniendo consentimientos pendientes");
        List<InformedConsentDTO> consents = informedConsentService.getPendingConsents();
        return ResponseEntity.ok(ApiResponse.success("Consentimientos pendientes obtenidos exitosamente", consents));
    }

    /**
     * Firmar consentimiento
     * PUT /api/informed-consents/{id}/sign
     */
    @PutMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InformedConsentDTO>> signConsent(
            @PathVariable Long id,
            @RequestParam String signature) {
        
        log.info("PUT /api/informed-consents/{}/sign - Firmando consentimiento", id);
        InformedConsentDTO consent = informedConsentService.signConsent(id, signature);
        return ResponseEntity.ok(ApiResponse.success("Consentimiento firmado exitosamente", consent));
    }

    /**
     * Eliminar consentimiento (soft delete)
     * DELETE /api/informed-consents/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteInformedConsent(@PathVariable Long id) {
        log.info("DELETE /api/informed-consents/{} - Eliminando consentimiento", id);
        informedConsentService.deleteInformedConsent(id);
        return ResponseEntity.ok(ApiResponse.success("Consentimiento eliminado exitosamente", null));
    }
}



