package com.vetclinic.patterns.strategy;

import com.vetclinic.entity.Appointment;

/**
 * Strategy Pattern - Reminder Strategy
 * Interfaz para diferentes estrategias de recordatorios de citas
 */
public interface ReminderStrategy {
    
    /**
     * Verificar si debe enviarse un recordatorio para esta cita
     * 
     * @param appointment La cita a verificar
     * @return true si debe enviarse el recordatorio, false en caso contrario
     */
    boolean shouldSendReminder(Appointment appointment);
    
    /**
     * Obtener el tiempo antes de la cita para enviar el recordatorio
     * 
     * @return Horas antes de la cita
     */
    int getHoursBefore();
    
    /**
     * Obtener el tipo de recordatorio
     * 
     * @return Tipo de recordatorio (24H, 1H, etc.)
     */
    String getReminderType();
}

