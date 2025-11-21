package com.vetclinic.patterns.strategy;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Strategy Pattern - Reminder 24 Hours Before
 * Estrategia para enviar recordatorios 24 horas antes de la cita
 */
@Component
@Slf4j
public class Reminder24HoursStrategy implements ReminderStrategy {

    private static final int HOURS_BEFORE = 24;

    @Override
    public boolean shouldSendReminder(Appointment appointment) {
        // Solo enviar recordatorios para citas confirmadas o programadas
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && 
            appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            return false;
        }

        // Verificar que la cita esté en el futuro
        if (appointment.getScheduledDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Calcular horas hasta la cita
        long hoursUntilAppointment = ChronoUnit.HOURS.between(
            LocalDateTime.now(), 
            appointment.getScheduledDate()
        );

        // Enviar recordatorio si faltan entre 24 y 25 horas (ventana de 1 hora)
        return hoursUntilAppointment >= HOURS_BEFORE && hoursUntilAppointment < HOURS_BEFORE + 1;
    }

    @Override
    public int getHoursBefore() {
        return HOURS_BEFORE;
    }

    @Override
    public String getReminderType() {
        return "24H";
    }
}

