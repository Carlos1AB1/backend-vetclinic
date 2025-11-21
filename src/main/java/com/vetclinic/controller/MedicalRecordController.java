package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.medicalrecord.CreateMedicalRecordRequest;
import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest;
import com.vetclinic.patterns.proxy.MedicalRecordServiceProxy;
import com.vetclinic.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de registros médicos
 */
@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@io.swagger.v3.oas.annotations.tags.Tag(name = "06. Historias Clínicas", description = "Gestión completa de historias clínicas - Registros médicos detallados de pacientes")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordServiceProxy medicalRecordServiceProxy;

    /**
     * Crear un nuevo registro médico
     * POST /api/medical-records
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> createMedicalRecord(@Valid @RequestBody CreateMedicalRecordRequest request) {
        log.info("POST /api/medical-records - Creando nuevo registro médico");
        MedicalRecordDTO medicalRecord = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registro médico creado exitosamente", medicalRecord));
    }

    /**
     * Obtener registros médicos con paginación
     * GET /api/medical-records/page?page=0&size=10&sortBy=recordDate&sortDirection=DESC
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<MedicalRecordDTO>>> getMedicalRecordsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /api/medical-records/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<MedicalRecordDTO> recordsPage = medicalRecordService.getMedicalRecordsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de registros médicos obtenida exitosamente", recordsPage));
    }

    /**
     * Obtener registro médico por ID
     * GET /api/medical-records/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> getMedicalRecordById(@PathVariable Long id) {
        log.info("GET /api/medical-records/{} - Obteniendo registro médico usando Proxy Pattern", id);
        // Usar Proxy Pattern para control de acceso y auditoría
        MedicalRecordDTO medicalRecord = medicalRecordServiceProxy.getMedicalRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Registro médico obtenido exitosamente", medicalRecord));
    }

    /**
     * Obtener registros médicos por paciente
     * GET /api/medical-records/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> getMedicalRecordsByPatient(@PathVariable Long patientId) {
        log.info("GET /api/medical-records/patient/{} - Obteniendo registros del paciente", patientId);
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Registros médicos obtenidos exitosamente", records));
    }

    /**
     * Obtener registros médicos por veterinario
     * GET /api/medical-records/veterinarian/{veterinarianId}
     */
    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> getMedicalRecordsByVeterinarian(@PathVariable UUID veterinarianId) {
        log.info("GET /api/medical-records/veterinarian/{} - Obteniendo registros del veterinario", veterinarianId);
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByVeterinarian(veterinarianId);
        return ResponseEntity.ok(ApiResponse.success("Registros médicos obtenidos exitosamente", records));
    }

    /**
     * Obtener registros médicos por rango de fechas
     * GET /api/medical-records/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> getMedicalRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("GET /api/medical-records/date-range - startDate: {}, endDate: {}", startDate, endDate);
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Registros médicos obtenidos exitosamente", records));
    }

    /**
     * Buscar registros por diagnóstico
     * GET /api/medical-records/search?diagnosis=diabetes&page=0&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<MedicalRecordDTO>>> searchByDiagnosis(
            @RequestParam String diagnosis,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/medical-records/search - diagnosis: {}", diagnosis);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        Page<MedicalRecordDTO> results = medicalRecordService.searchByDiagnosis(diagnosis, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada exitosamente", results));
    }

    /**
     * Obtener registros que requieren seguimiento
     * GET /api/medical-records/follow-up
     */
    @GetMapping("/follow-up")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> getRecordsRequiringFollowUp() {
        log.info("GET /api/medical-records/follow-up - Obteniendo registros que requieren seguimiento");
        List<MedicalRecordDTO> records = medicalRecordService.getRecordsRequiringFollowUp();
        return ResponseEntity.ok(ApiResponse.success("Registros obtenidos exitosamente", records));
    }

    /**
     * Actualizar registro médico
     * PUT /api/medical-records/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicalRecordRequest request) {
        
        log.info("PUT /api/medical-records/{} - Actualizando registro médico usando Proxy Pattern", id);
        // Usar Proxy Pattern para control de acceso y auditoría
        MedicalRecordDTO medicalRecord = medicalRecordServiceProxy.updateMedicalRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success("Registro médico actualizado exitosamente", medicalRecord));
    }

    /**
     * Eliminar registro médico (soft delete)
     * DELETE /api/medical-records/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMedicalRecord(@PathVariable Long id) {
        log.info("DELETE /api/medical-records/{} - Eliminando registro médico usando Proxy Pattern", id);
        // Usar Proxy Pattern para control de acceso y auditoría
        medicalRecordServiceProxy.deleteMedicalRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Registro médico eliminado exitosamente", null));
    }

    /**
     * Contar registros médicos activos
     * GET /api/medical-records/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countMedicalRecords() {
        log.info("GET /api/medical-records/count - Contando registros médicos activos");
        long count = medicalRecordService.countActiveMedicalRecords();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }
}
