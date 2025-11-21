package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State Pattern
 * Estado: CANCELLED (Cancelada)
 */
@Component
@Slf4j
public class CancelledState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        throw new IllegalStateException("No se puede confirmar una cita cancelada");
    }

    @Override
    public void cancel(Appointment appointment) {
        log.warn("La cita ID: {} ya está cancelada", appointment.getId());
        // Ya está cancelada, no hacer nada
    }

    @Override
    public void start(Appointment appointment) {
        throw new IllegalStateException("No se puede iniciar una cita cancelada");
    }

    @Override
    public void complete(Appointment appointment) {
        throw new IllegalStateException("No se puede completar una cita cancelada");
    }

    @Override
    public String getStateName() {
        return "CANCELLED";
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

