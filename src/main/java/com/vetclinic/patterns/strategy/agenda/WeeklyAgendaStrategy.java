package com.vetclinic.patterns.strategy.agenda;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.entity.Appointment;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Strategy Pattern
 * Estrategia para vista semanal de agenda
 * RF010 - Visualización de Agenda
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyAgendaStrategy implements AgendaViewStrategy {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Override
    public List<AppointmentDTO> getAppointments(LocalDateTime startDate, LocalDateTime endDate, UUID veterinarianId) {
        log.debug("Obteniendo vista semanal de agenda desde: {} hasta: {}", startDate, endDate);
        
        LocalDateTime weekStart = startDate.toLocalDate()
            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .atStartOfDay();
        
        LocalDateTime weekEnd = weekStart.plusDays(7).minusSeconds(1);
        
        List<Appointment> appointments;
        if (veterinarianId != null) {
            appointments = appointmentRepository.findByVeterinarianAndDateRange(veterinarianId, weekStart, weekEnd);
        } else {
            appointments = appointmentRepository.findByDateRange(weekStart, weekEnd);
        }
        
        return appointments.stream()
            .map(appointment -> appointmentService.getAppointmentById(appointment.getId()))
            .collect(Collectors.toList());
    }

    @Override
    public String getViewType() {
        return "WEEKLY";
    }

    @Override
    public String getDescription() {
        return "Vista semanal de la agenda";
    }
}



