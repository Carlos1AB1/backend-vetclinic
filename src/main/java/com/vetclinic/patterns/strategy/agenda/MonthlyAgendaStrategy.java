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
 * Estrategia para vista mensual de agenda
 * RF010 - Visualización de Agenda
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyAgendaStrategy implements AgendaViewStrategy {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Override
    public List<AppointmentDTO> getAppointments(LocalDateTime startDate, LocalDateTime endDate, UUID veterinarianId) {
        log.debug("Obteniendo vista mensual de agenda para mes: {}", startDate.toLocalDate().getMonth());
        
        LocalDateTime monthStart = startDate.toLocalDate()
            .with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay();
        
        LocalDateTime monthEnd = startDate.toLocalDate()
            .with(TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59);
        
        List<Appointment> appointments;
        if (veterinarianId != null) {
            appointments = appointmentRepository.findByVeterinarianAndDateRange(veterinarianId, monthStart, monthEnd);
        } else {
            appointments = appointmentRepository.findByDateRange(monthStart, monthEnd);
        }
        
        return appointments.stream()
            .map(appointment -> appointmentService.getAppointmentById(appointment.getId()))
            .collect(Collectors.toList());
    }

    @Override
    public String getViewType() {
        return "MONTHLY";
    }

    @Override
    public String getDescription() {
        return "Vista mensual de la agenda";
    }
}



