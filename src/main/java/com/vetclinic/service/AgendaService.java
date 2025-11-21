package com.vetclinic.service;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.patterns.strategy.agenda.AgendaViewStrategy;
import com.vetclinic.patterns.strategy.agenda.DailyAgendaStrategy;
import com.vetclinic.patterns.strategy.agenda.MonthlyAgendaStrategy;
import com.vetclinic.patterns.strategy.agenda.WeeklyAgendaStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para visualización de agenda con diferentes vistas
 * RF010 - Visualización de Agenda
 * Usa Strategy Pattern para diferentes tipos de vistas (diaria, semanal, mensual)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgendaService {

    private final DailyAgendaStrategy dailyStrategy;
    private final WeeklyAgendaStrategy weeklyStrategy;
    private final MonthlyAgendaStrategy monthlyStrategy;

    /**
     * Obtener vista de agenda según el tipo especificado
     * 
     * @param viewType Tipo de vista: DAILY, WEEKLY, MONTHLY
     * @param date Fecha de referencia
     * @param veterinarianId ID del veterinario (opcional)
     * @return Lista de citas según la vista
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAgendaView(String viewType, LocalDateTime date, UUID veterinarianId) {
        log.info("Obteniendo vista de agenda: {} para fecha: {}", viewType, date);

        AgendaViewStrategy strategy = getStrategy(viewType);
        LocalDateTime endDate = calculateEndDate(viewType, date);

        return strategy.getAppointments(date, endDate, veterinarianId);
    }

    /**
     * Obtener vista diaria
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDailyView(LocalDateTime date, UUID veterinarianId) {
        return getAgendaView("DAILY", date, veterinarianId);
    }

    /**
     * Obtener vista semanal
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getWeeklyView(LocalDateTime date, UUID veterinarianId) {
        return getAgendaView("WEEKLY", date, veterinarianId);
    }

    /**
     * Obtener vista mensual
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getMonthlyView(LocalDateTime date, UUID veterinarianId) {
        return getAgendaView("MONTHLY", date, veterinarianId);
    }

    /**
     * Obtener la estrategia según el tipo de vista
     */
    private AgendaViewStrategy getStrategy(String viewType) {
        if (viewType == null || viewType.trim().isEmpty()) {
            log.warn("Tipo de vista no especificado, usando DAILY por defecto");
            return dailyStrategy;
        }

        String normalizedType = viewType.toUpperCase().trim();

        return switch (normalizedType) {
            case "DAILY", "DAY" -> dailyStrategy;
            case "WEEKLY", "WEEK" -> weeklyStrategy;
            case "MONTHLY", "MONTH" -> monthlyStrategy;
            default -> {
                log.warn("Tipo de vista no soportado: {}. Usando DAILY por defecto", viewType);
                yield dailyStrategy;
            }
        };
    }

    /**
     * Calcular fecha de fin según el tipo de vista
     */
    private LocalDateTime calculateEndDate(String viewType, LocalDateTime startDate) {
        String normalizedType = viewType.toUpperCase().trim();

        return switch (normalizedType) {
            case "DAILY", "DAY" -> startDate.plusDays(1);
            case "WEEKLY", "WEEK" -> startDate.plusWeeks(1);
            case "MONTHLY", "MONTH" -> startDate.plusMonths(1);
            default -> startDate.plusDays(1);
        };
    }
}



