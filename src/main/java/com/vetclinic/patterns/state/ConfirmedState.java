package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State Pattern
 * Estado: CONFIRMED (Confirmada)
 */
@Component
@Slf4j
public class ConfirmedState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        log.warn("La cita ID: {} ya está confirmada", appointment.getId());
        // Ya está confirmada, no hacer nada
    }

    @Override
    public void cancel(Appointment appointment) {
        log.info("Cancelando cita ID: {} desde estado CONFIRMED", appointment.getId());
        appointment.setStatus(AppointmentStatus.CANCELLED);
    }

    @Override
    public void start(Appointment appointment) {
        log.info("Iniciando cita ID: {} desde estado CONFIRMED", appointment.getId());
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
    }

    @Override
    public void complete(Appointment appointment) {
        throw new IllegalStateException("No se puede completar una cita que está en estado CONFIRMED. Debe iniciarse primero.");
    }

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }

    @Override
    public boolean canSendReminders() {
        return true;
    }

    @Override
    public boolean canBeRescheduled() {
        return true;
    }
}

