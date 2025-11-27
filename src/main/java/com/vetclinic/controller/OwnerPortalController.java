package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.service.ServiceDTO;
import com.vetclinic.entity.User;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.OwnerPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para el portal de propietarios
 */
@RestController
@RequestMapping("/owner-portal")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Owner Portal", description = "Portal de propietarios - Gestión de citas, mascotas y servicios")
@SecurityRequirement(name = "bearerAuth")
public class OwnerPortalController {

    private final OwnerPortalService ownerPortalService;
    private final UserRepository userRepository;

    /**
     * Obtiene el UUID del usuario desde el username de authentication
     */
    private String getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return user.getId().toString();
    }

    /**
     * Obtener todas las citas del propietario
     * GET /api/owner-portal/my-appointments
     */
    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Mis citas", description = "Obtiene todas las citas del propietario autenticado")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getMyAppointments(Authentication authentication) {
        log.info("GET /api/owner-portal/my-appointments - Usuario: {}", authentication.getName());
        String userId = getUserIdFromAuthentication(authentication);
        List<AppointmentDTO> appointments = ownerPortalService.getMyAppointments(userId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Crear una cita
     * POST /api/owner-portal/appointments
     */
    @PostMapping("/appointments")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Reservar cita", description = "Crea una nueva cita para una mascota del propietario")
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            Authentication authentication) {
        log.info("POST /api/owner-portal/appointments - Creando cita para usuario: {}", authentication.getName());
        String userId = getUserIdFromAuthentication(authentication);
        AppointmentDTO appointment = ownerPortalService.createAppointment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cita creada exitosamente", appointment));
    }

    /**
     * Cancelar una cita
     * PUT /api/owner-portal/appointments/{id}/cancel
     */
    @PutMapping("/appointments/{id}/cancel")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cancelar cita", description = "Cancela una cita del propietario")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PUT /api/owner-portal/appointments/{}/cancel - Usuario: {}", id, authentication.getName());
        String userId = getUserIdFromAuthentication(authentication);
        AppointmentDTO appointment = ownerPortalService.cancelAppointment(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Cita cancelada exitosamente", appointment));
    }

    /**
     * Reprogramar una cita
     * PUT /api/owner-portal/appointments/{id}/reschedule
     */
    @PutMapping("/appointments/{id}/reschedule")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Reprogramar cita", description = "Reprograma una cita del propietario")
    public ResponseEntity<ApiResponse<AppointmentDTO>> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody CreateAppointmentRequest request,
            Authentication authentication) {
        log.info("PUT /api/owner-portal/appointments/{}/reschedule - Usuario: {}", id, authentication.getName());
        String userId = getUserIdFromAuthentication(authentication);
        AppointmentDTO appointment = ownerPortalService.rescheduleAppointment(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada exitosamente", appointment));
    }

    /**
     * Obtener todas las mascotas del propietario
     * GET /api/owner-portal/my-pets
     */
    @GetMapping("/my-pets")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Mis mascotas", description = "Obtiene todas las mascotas del propietario")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getMyPets(Authentication authentication) {
        log.info("GET /api/owner-portal/my-pets - Usuario: {}", authentication.getName());
        String userId = getUserIdFromAuthentication(authentication);
        List<PatientDTO> pets = ownerPortalService.getMyPets(userId);
        return ResponseEntity.ok(ApiResponse.success("Mascotas obtenidas exitosamente", pets));
    }

    /**
     * Obtener servicios disponibles
     * GET /api/owner-portal/services
     */
    @GetMapping("/services")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Servicios disponibles", description = "Obtiene todos los servicios disponibles en la clínica")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAvailableServices() {
        log.info("GET /api/owner-portal/services - Obteniendo servicios disponibles");
        List<ServiceDTO> services = ownerPortalService.getAvailableServices();
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos exitosamente", services));
    }
}