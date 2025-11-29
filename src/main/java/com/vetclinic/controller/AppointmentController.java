package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.appointment.RescheduleAppointmentRequest;
import com.vetclinic.dto.appointment.UpdateAppointmentRequest;
import com.vetclinic.patterns.facade.ClinicaFacade;
import com.vetclinic.service.AppointmentActionTokenService;
import com.vetclinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de citas
 */
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "05. Citas Médicas", description = "Sistema completo de agendamiento y gestión de citas - Incluye validaciones y notificaciones")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClinicaFacade clinicaFacade;
    private final AppointmentActionTokenService tokenService;

    /**
     * Crear una nueva cita
     * POST /api/appointments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        log.info("POST /api/appointments - Creando nueva cita usando Facade Pattern");
        // Usar Facade Pattern para agendar cita completa
        AppointmentDTO appointment = clinicaFacade.agendarCita(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cita creada exitosamente", appointment));
    }

    /**
     * Obtener citas con paginación
     * GET /api/appointments/page?page=0&size=10&sortBy=scheduledDate&sortDirection=DESC
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<AppointmentDTO>>> getAppointmentsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /api/appointments/page - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AppointmentDTO> appointmentsPage = appointmentService.getAppointmentsPage(pageable);
        return ResponseEntity.ok(ApiResponse.success("Página de citas obtenida exitosamente", appointmentsPage));
    }

    /**
     * Obtener cita por ID
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(@PathVariable Long id) {
        log.info("GET /api/appointments/{} - Obteniendo cita", id);
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Cita obtenida exitosamente", appointment));
    }

    /**
     * Obtener citas por paciente
     * GET /api/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByPatient(@PathVariable Long patientId) {
        log.info("GET /api/appointments/patient/{} - Obteniendo citas del paciente", patientId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Obtener citas por propietario
     * GET /api/appointments/owner/{ownerId}
     */
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByOwner(@PathVariable Long ownerId) {
        log.info("GET /api/appointments/owner/{} - Obteniendo citas del propietario", ownerId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Obtener citas por veterinario
     * GET /api/appointments/veterinarian/{veterinarianId}
     */
    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByVeterinarian(@PathVariable UUID veterinarianId) {
        log.info("GET /api/appointments/veterinarian/{} - Obteniendo citas del veterinario", veterinarianId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByVeterinarian(veterinarianId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Obtener citas por rango de fechas
     * GET /api/appointments/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("GET /api/appointments/date-range - startDate: {}, endDate: {}", startDate, endDate);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Obtener citas por fecha específica
     * GET /api/appointments/date?date=2024-01-01
     */
    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("GET /api/appointments/date - date: {}", date);
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", appointments));
    }

    /**
     * Obtener citas próximas (7 días)
     * GET /api/appointments/upcoming
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getUpcomingAppointments() {
        log.info("GET /api/appointments/upcoming - Obteniendo citas próximas");
        List<AppointmentDTO> appointments = appointmentService.getUpcomingAppointments();
        return ResponseEntity.ok(ApiResponse.success("Citas próximas obtenidas exitosamente", appointments));
    }

    /**
     * Actualizar cita
     * PUT /api/appointments/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        
        log.info("PUT /api/appointments/{} - Actualizando cita", id);
        AppointmentDTO appointment = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cita actualizada exitosamente", appointment));
    }

    /**
     * Cancelar cita
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'VETERINARIAN')")
    public ResponseEntity<ApiResponse<String>> cancelAppointment(@PathVariable Long id) {
        log.error("═══════════════════════════════════════════════════════════════");
        log.error("🔴 ENDPOINT CANCELAR CITA LLAMADO");
        log.error("   Path: PUT /api/appointments/{}/cancel", id);
        log.error("   Cita ID: {}", id);
        log.error("═══════════════════════════════════════════════════════════════");
        
        // Usar Facade Pattern para cancelar cita completa
        clinicaFacade.cancelarCita(id);
        
        log.error("✅ Facade.cancelarCita() ejecutado - Retornando respuesta");
        return ResponseEntity.ok(ApiResponse.success("Cita cancelada exitosamente", null));
    }

    /**
     * Eliminar cita (soft delete)
     * DELETE /api/appointments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAppointment(@PathVariable Long id) {
        log.info("DELETE /api/appointments/{} - Eliminando cita", id);
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Cita eliminada exitosamente", null));
    }

    /**
     * Contar citas activas
     * GET /api/appointments/count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Long>> countAppointments() {
        log.info("GET /api/appointments/count - Contando citas activas");
        long count = appointmentService.countActiveAppointments();
        return ResponseEntity.ok(ApiResponse.success("Conteo realizado exitosamente", count));
    }

    // ========== ENDPOINTS PÚBLICOS PARA ACCIONES DESDE RECORDATORIOS ==========
    // RF018 - Recordatorio de Citas: Permitir confirmar, cancelar o reprogramar desde recordatorio

    /**
     * Confirmar cita desde recordatorio (público con token)
     * GET /api/appointments/{id}/confirm?token=...
     */
    @GetMapping("/{id}/confirm")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Confirmar cita desde recordatorio",
        description = "Confirma una cita usando el token del recordatorio. Endpoint público."
    )
    public ResponseEntity<ApiResponse<AppointmentDTO>> confirmAppointmentFromReminder(
            @PathVariable Long id,
            @RequestParam String token) {
        log.info("GET /api/appointments/{}/confirm - Confirmando cita desde recordatorio", id);
        
        // Validar token y ejecutar acción
        AppointmentDTO appointment = appointmentService.confirmAppointmentFromReminder(id, token);
        return ResponseEntity.ok(ApiResponse.success("Cita confirmada exitosamente", appointment));
    }

    /**
     * Cancelar cita desde recordatorio (público con token)
     * GET /api/appointments/{id}/cancel?token=...
     */
    @GetMapping("/{id}/cancel-reminder")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Cancelar cita desde recordatorio",
        description = "Cancela una cita usando el token del recordatorio. Endpoint público."
    )
    public ResponseEntity<ApiResponse<String>> cancelAppointmentFromReminder(
            @PathVariable Long id,
            @RequestParam String token) {
        log.info("GET /api/appointments/{}/cancel-reminder - Cancelando cita desde recordatorio", id);
        
        // Validar token y ejecutar acción
        appointmentService.cancelAppointmentFromReminder(id, token);
        return ResponseEntity.ok(ApiResponse.success("Cita cancelada exitosamente", null));
    }

    /**
     * Reprogramar cita desde recordatorio (público con token)
     * POST /api/appointments/{id}/reschedule?token=...
     */
    @PostMapping("/{id}/reschedule")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Reprogramar cita desde recordatorio",
        description = "Reprograma una cita usando el token del recordatorio. Endpoint público."
    )
    public ResponseEntity<ApiResponse<AppointmentDTO>> rescheduleAppointmentFromReminder(
            @PathVariable Long id,
            @RequestParam String token,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        log.info("POST /api/appointments/{}/reschedule - Reprogramando cita desde recordatorio", id);
        
        // Validar token y ejecutar acción
        AppointmentDTO appointment = appointmentService.rescheduleAppointmentFromReminder(id, token, request);
        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada exitosamente", appointment));
    }
}
