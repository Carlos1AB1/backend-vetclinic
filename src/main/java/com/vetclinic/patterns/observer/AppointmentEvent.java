package com.vetclinic.patterns.observer;

import com.vetclinic.entity.Appointment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern
 * Evento que se dispara cuando cambia el estado de una cita
 */
@Getter
public class AppointmentEvent extends ApplicationEvent {

    private final Appointment appointment;
    private final AppointmentEventType eventType;
    private final String previousStatus;

    public AppointmentEvent(Object source, Appointment appointment, AppointmentEventType eventType, String previousStatus) {
        super(source);
        this.appointment = appointment;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
    }

    public enum AppointmentEventType {
        CREATED,
        CONFIRMED,
        CANCELLED,
        COMPLETED,
        STATUS_CHANGED
    }
}

