package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State Pattern
 * Estado: COMPLETED (Completada)
 */
@Component
@Slf4j
public class CompletedState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        throw new IllegalStateException("No se puede confirmar una cita completada");
    }

    @Override
    public void cancel(Appointment appointment) {
        throw new IllegalStateException("No se puede cancelar una cita completada");
    }

    @Override
    public void start(Appointment appointment) {
        throw new IllegalStateException("No se puede iniciar una cita completada");
    }

    @Override
    public void complete(Appointment appointment) {
        log.warn("La cita ID: {} ya está completada", appointment.getId());
        // Ya está completada, no hacer nada
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }

    @Override
    public boolean canSendReminders() {
        return false;
    }

    @Override
    public boolean canBeRescheduled() {
        return false;
    }
}

