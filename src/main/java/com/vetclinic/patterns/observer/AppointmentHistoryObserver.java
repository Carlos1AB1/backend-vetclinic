package com.vetclinic.patterns.observer;

import com.vetclinic.entity.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern
 * Observador que registra cambios en el historial de citas
 */
@Component
@Slf4j
public class AppointmentHistoryObserver {

    @EventListener
    @Async
    public void handleAppointmentEvent(AppointmentEvent event) {
        Appointment appointment = event.getAppointment();
        AppointmentEvent.AppointmentEventType eventType = event.getEventType();

        log.info("Registrando cambio en historial - Cita ID: {}, Evento: {}, Estado anterior: {}, Estado actual: {}",
            appointment.getId(),
            eventType,
            event.getPreviousStatus(),
            appointment.getStatus()
        );

        // Aquí se podría guardar en una tabla de auditoría/historial
        // Por ahora solo lo registramos en logs
    }
}

