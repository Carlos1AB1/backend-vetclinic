package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State Pattern
 * Estado: SCHEDULED (Programada)
 */
@Component
@Slf4j
public class ScheduledState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        log.info("Confirmando cita ID: {} desde estado SCHEDULED", appointment.getId());
        appointment.setStatus(AppointmentStatus.CONFIRMED);
    }

    @Override
    public void cancel(Appointment appointment) {
        log.info("Cancelando cita ID: {} desde estado SCHEDULED", appointment.getId());
        appointment.setStatus(AppointmentStatus.CANCELLED);
    }

    @Override
    public void start(Appointment appointment) {
        log.info("Iniciando cita ID: {} desde estado SCHEDULED", appointment.getId());
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
    }

    @Override
    public void complete(Appointment appointment) {
        throw new IllegalStateException("No se puede completar una cita que está en estado SCHEDULED. Debe iniciarse primero.");
    }

    @Override
    public String getStateName() {
        return "SCHEDULED";
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

