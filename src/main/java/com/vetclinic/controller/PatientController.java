package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.patient.CreatePatientRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.patient.UpdatePatientRequest;
import com.vetclinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * Controlador REST para la gestión de pacientes
 * Implementa patrón Controller de Spring MVC
 */
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "03. Pacientes (Mascotas)", description = "Gestión completa de pacientes (mascotas) - Registro, búsqueda y actualización")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    /**
     * Crear un nuevo paciente
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(
        summary = "Crear nuevo paciente (mascota)",
        description = "Registra un nuevo paciente en el sistema con todos sus datos básicos"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Paciente creado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Propietario no encontrado")
    })
    public ResponseEntity<ApiResponse<PatientDTO>> createPatient(
        @Parameter(description = "Datos del nuevo paciente", required = true)
        @Valid @RequestBody CreatePatientRequest request
    ) {
        PatientDTO patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(patient));
    }

    /**
     * Obtener todos los pacientes activos con paginación
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(
        summary = "Listar todos los pacientes",
        description = "Obtiene lista paginada de todos los pacientes activos con opciones de ordenamiento"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    public ResponseEntity<ApiResponse<Page<PatientDTO>>> getAllPatients(
            @Parameter(description = "Número de página (inicia en 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "name") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "ASC") @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PatientDTO> patientsPage = patientService.getPatientsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success(patientsPage));
    }

    /**
     * Obtener pacientes con paginación
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(summary = "Listar pacientes paginado", description = "Obtiene pacientes con paginación y ordenamiento")
    public ResponseEntity<ApiResponse<Page<PatientDTO>>> getPatientsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PatientDTO> patientsPage = patientService.getPatientsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success(patientsPage));
    }

    /**
     * Obtener paciente por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(summary = "Obtener paciente", description = "Obtiene un paciente específico por su ID")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    /**
     * Actualizar paciente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    @Operation(summary = "Actualizar paciente", description = "Actualiza la información de un paciente existente")
    public ResponseEntity<ApiResponse<PatientDTO>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        PatientDTO updatedPatient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedPatient));
    }

    /**
     * Eliminar paciente (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar paciente", description = "Desactiva un paciente del sistema")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Paciente eliminado exitosamente")
            .build());
    }

    /**
     * Buscar pacientes por nombre
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(summary = "Buscar pacientes", description = "Busca pacientes por nombre")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> searchPatients(@RequestParam String name) {
        List<PatientDTO> patients = patientService.searchPatientsByName(name);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    /**
     * Obtener pacientes por propietario
     */
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(summary = "Pacientes por propietario", description = "Obtiene todos los pacientes de un propietario")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getPatientsByOwner(@PathVariable Long ownerId) {
        List<PatientDTO> patients = patientService.getPatientsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    /**
     * Obtener pacientes por especie
     */
    @GetMapping("/species/{species}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    @Operation(summary = "Pacientes por especie", description = "Obtiene pacientes filtrados por especie")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getPatientsBySpecies(@PathVariable String species) {
        List<PatientDTO> patients = patientService.getPatientsBySpecies(species);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    /**
     * Contar pacientes activos
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    @Operation(summary = "Contar pacientes", description = "Obtiene el total de pacientes activos")
    public ResponseEntity<ApiResponse<Long>> countActivePatients() {
        long count = patientService.countActivePatients();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
