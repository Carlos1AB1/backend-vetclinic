package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.service.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para visualización de agenda
 * RF010 - Visualización de Agenda
 * Usa Strategy Pattern para diferentes tipos de vistas
 */
@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AgendaController {

    private final AgendaService agendaService;

    /**
     * Obtener vista de agenda según el tipo
     * GET /api/agenda/view?type=DAILY&date=2024-01-15&veterinarianId=...
     */
    @GetMapping("/view")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAgendaView(
            @RequestParam(defaultValue = "DAILY") String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID veterinarianId) {
        
        log.info("GET /api/agenda/view - Tipo: {}, Fecha: {}, Veterinario: {}", type, date, veterinarianId);
        
        LocalDateTime dateTime = date.atStartOfDay();
        List<AppointmentDTO> appointments = agendaService.getAgendaView(type, dateTime, veterinarianId);
        
        return ResponseEntity.ok(ApiResponse.success("Vista de agenda obtenida exitosamente", appointments));
    }

    /**
     * Obtener vista diaria
     * GET /api/agenda/daily?date=2024-01-15&veterinarianId=...
     */
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDailyView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID veterinarianId) {
        
        log.info("GET /api/agenda/daily - Fecha: {}, Veterinario: {}", date, veterinarianId);
        
        LocalDateTime dateTime = date.atStartOfDay();
        List<AppointmentDTO> appointments = agendaService.getDailyView(dateTime, veterinarianId);
        
        return ResponseEntity.ok(ApiResponse.success("Vista diaria obtenida exitosamente", appointments));
    }

    /**
     * Obtener vista semanal
     * GET /api/agenda/weekly?date=2024-01-15&veterinarianId=...
     */
    @GetMapping("/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getWeeklyView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID veterinarianId) {
        
        log.info("GET /api/agenda/weekly - Fecha: {}, Veterinario: {}", date, veterinarianId);
        
        LocalDateTime dateTime = date.atStartOfDay();
        List<AppointmentDTO> appointments = agendaService.getWeeklyView(dateTime, veterinarianId);
        
        return ResponseEntity.ok(ApiResponse.success("Vista semanal obtenida exitosamente", appointments));
    }

    /**
     * Obtener vista mensual
     * GET /api/agenda/monthly?date=2024-01-15&veterinarianId=...
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getMonthlyView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID veterinarianId) {
        
        log.info("GET /api/agenda/monthly - Fecha: {}, Veterinario: {}", date, veterinarianId);
        
        LocalDateTime dateTime = date.atStartOfDay();
        List<AppointmentDTO> appointments = agendaService.getMonthlyView(dateTime, veterinarianId);
        
        return ResponseEntity.ok(ApiResponse.success("Vista mensual obtenida exitosamente", appointments));
    }
}



