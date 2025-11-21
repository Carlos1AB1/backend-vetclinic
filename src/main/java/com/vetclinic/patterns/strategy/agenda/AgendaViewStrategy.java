package com.vetclinic.patterns.strategy.agenda;

import com.vetclinic.dto.appointment.AppointmentDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Strategy Pattern
 * Interfaz para diferentes estrategias de visualización de agenda
 * RF010 - Visualización de Agenda
 */
public interface AgendaViewStrategy {
    
    /**
     * Obtener citas según la estrategia de vista
     * 
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param veterinarianId ID del veterinario (opcional, null para todos)
     * @return Lista de citas
     */
    List<AppointmentDTO> getAppointments(LocalDateTime startDate, LocalDateTime endDate, java.util.UUID veterinarianId);
    
    /**
     * Obtener el tipo de vista
     */
    String getViewType();
    
    /**
     * Obtener la descripción de la vista
     */
    String getDescription();
}



