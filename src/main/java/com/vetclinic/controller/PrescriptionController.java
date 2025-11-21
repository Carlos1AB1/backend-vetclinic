package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.prescription.CreatePrescriptionRequest;
import com.vetclinic.dto.prescription.PrescriptionDTO;
import com.vetclinic.dto.prescription.UpdatePrescriptionRequest;
import com.vetclinic.service.PrescriptionExportService;
import com.vetclinic.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Controlador REST para la gestión de prescripciones
 */
@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@io.swagger.v3.oas.annotations.tags.Tag(name = "07. Recetas y Prescripciones", description = "Gestión de prescripciones médicas - Incluye exportación a PDF y Excel")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionExportService prescriptionExportService;

    /**
     * Crear una nueva prescripción
     * POST /api/prescriptions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<PrescriptionDTO>> createPrescription(@Valid @RequestBody CreatePrescriptionRequest request) {
        log.info("POST /api/prescriptions - Creando nueva prescripción");
        PrescriptionDTO prescription = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescripción creada exitosamente", prescription));
    }

    /**
     * Obtener prescripciones con paginación
     * GET /api/prescriptions/page?page=0&size=10&sortBy=startDate&sortDirection=DESC
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<PrescriptionDTO>>> getPrescriptionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /api/prescriptions/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PrescriptionDTO> prescriptionsPage = prescriptionService.getPrescriptionsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de prescripciones obtenida exitosamente", prescriptionsPage));
    }

    /**
     * Obtener prescripción por ID
     * GET /api/prescriptions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PrescriptionDTO>> getPrescriptionById(@PathVariable Long id) {
        log.info("GET /api/prescriptions/{} - Obteniendo prescripción", id);
        PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Prescripción obtenida exitosamente", prescription));
    }

    /**
     * Obtener prescripciones por paciente
     * GET /api/prescriptions/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PrescriptionDTO>>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        log.info("GET /api/prescriptions/patient/{} - Obteniendo prescripciones del paciente", patientId);
        List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Prescripciones obtenidas exitosamente", prescriptions));
    }

    /**
     * Obtener prescripciones activas por paciente
     * GET /api/prescriptions/patient/{patientId}/active
     */
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PrescriptionDTO>>> getActivePrescriptionsByPatient(@PathVariable Long patientId) {
        log.info("GET /api/prescriptions/patient/{}/active - Obteniendo prescripciones activas", patientId);
        List<PrescriptionDTO> prescriptions = prescriptionService.getActivePrescriptionsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Prescripciones activas obtenidas exitosamente", prescriptions));
    }

    /**
     * Obtener prescripciones por registro médico
     * GET /api/prescriptions/medical-record/{medicalRecordId}
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PrescriptionDTO>>> getPrescriptionsByMedicalRecord(@PathVariable Long medicalRecordId) {
        log.info("GET /api/prescriptions/medical-record/{} - Obteniendo prescripciones del registro", medicalRecordId);
        List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByMedicalRecord(medicalRecordId);
        return ResponseEntity.ok(ApiResponse.success("Prescripciones obtenidas exitosamente", prescriptions));
    }

    /**
     * Buscar prescripciones por medicamento
     * GET /api/prescriptions/search?medication=aspirina&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<PrescriptionDTO>>> searchByMedication(
            @RequestParam String medication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/prescriptions/search - medication: {}", medication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<PrescriptionDTO> results = prescriptionService.searchByMedication(medication, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada exitosamente", results));
    }

    /**
     * Obtener prescripciones que expiran pronto
     * GET /api/prescriptions/expiring
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PrescriptionDTO>>> getExpiringPrescriptions() {
        log.info("GET /api/prescriptions/expiring - Obteniendo prescripciones que expiran pronto");
        List<PrescriptionDTO> prescriptions = prescriptionService.getExpiringPrescriptions();
        return ResponseEntity.ok(ApiResponse.success("Prescripciones obtenidas exitosamente", prescriptions));
    }

    /**
     * Actualizar prescripción
     * PUT /api/prescriptions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<PrescriptionDTO>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescriptionRequest request) {
        
        log.info("PUT /api/prescriptions/{} - Actualizando prescripción", id);
        PrescriptionDTO prescription = prescriptionService.updatePrescription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Prescripción actualizada exitosamente", prescription));
    }

    /**
     * Eliminar prescripción (soft delete)
     * DELETE /api/prescriptions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deletePrescription(@PathVariable Long id) {
        log.info("DELETE /api/prescriptions/{} - Eliminando prescripción", id);
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success("Prescripción eliminada exitosamente", null));
    }

    /**
     * Contar prescripciones activas
     * GET /api/prescriptions/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countPrescriptions() {
        log.info("GET /api/prescriptions/count - Contando prescripciones activas");
        long count = prescriptionService.countActivePrescriptions();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }

    /**
     * Exportar receta en formato PDF o Excel
     * GET /api/prescriptions/{id}/export?format=PDF
     * RF014 - Generación de Recetas
     */
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<byte[]> exportPrescription(
            @PathVariable Long id,
            @RequestParam(defaultValue = "PDF") String format) {
        
        log.info("GET /api/prescriptions/{}/export - Formato: {}", id, format);
        
        // Usar Factory Method Pattern para exportar en el formato solicitado
        ByteArrayOutputStream outputStream = prescriptionExportService.exportPrescription(id, format);
        
        String fileExtension = prescriptionExportService.getFileExtension(format);
        String mimeType = prescriptionExportService.getMimeType(format);
        String filename = "receta_" + id + fileExtension;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
