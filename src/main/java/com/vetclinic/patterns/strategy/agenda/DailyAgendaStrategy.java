package com.vetclinic.patterns.strategy.agenda;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.entity.Appointment;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Strategy Pattern
 * Estrategia para vista diaria de agenda
 * RF010 - Visualización de Agenda
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAgendaStrategy implements AgendaViewStrategy {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Override
    public List<AppointmentDTO> getAppointments(LocalDateTime startDate, LocalDateTime endDate, UUID veterinarianId) {
        log.debug("Obteniendo vista diaria de agenda para fecha: {}", startDate.toLocalDate());
        
        LocalDateTime startOfDay = startDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startDate.toLocalDate().atTime(LocalTime.MAX);
        
        List<Appointment> appointments;
        if (veterinarianId != null) {
            appointments = appointmentRepository.findTodayAppointments(veterinarianId, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDateRange(startOfDay, endOfDay);
        }
        
        return appointments.stream()
            .map(appointment -> mapToDTO(appointment))
            .collect(Collectors.toList());
    }

    @Override
    public String getViewType() {
        return "DAILY";
    }

    @Override
    public String getDescription() {
        return "Vista diaria de la agenda";
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        // Usar el método del servicio para mapear
        return appointmentService.getAppointmentById(appointment.getId());
    }
}



