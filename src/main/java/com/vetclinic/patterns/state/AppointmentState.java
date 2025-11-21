package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;

/**
 * State Pattern
 * Interfaz para los diferentes estados de una cita
 */
public interface AppointmentState {
    
    /**
     * Confirmar la cita
     */
    void confirm(Appointment appointment);
    
    /**
     * Cancelar la cita
     */
    void cancel(Appointment appointment);
    
    /**
     * Marcar como en progreso
     */
    void start(Appointment appointment);
    
    /**
     * Completar la cita
     */
    void complete(Appointment appointment);
    
    /**
     * Obtener el nombre del estado
     */
    String getStateName();
    
    /**
     * Verificar si se pueden enviar recordatorios en este estado
     */
    boolean canSendReminders();
    
    /**
     * Verificar si la cita puede ser reprogramada
     */
    boolean canBeRescheduled();
}

