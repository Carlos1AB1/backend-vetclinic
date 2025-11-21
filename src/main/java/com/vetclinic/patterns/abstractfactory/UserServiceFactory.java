package com.vetclinic.patterns.abstractfactory;

import com.vetclinic.service.AppointmentService;
import com.vetclinic.service.MedicalRecordService;
import com.vetclinic.service.PatientService;

/**
 * Abstract Factory Pattern
 * Fábrica abstracta para crear familias de servicios según el rol del usuario
 */
public interface UserServiceFactory {
    
    /**
     * Crear servicio de citas específico para el rol
     */
    AppointmentService createAppointmentService();
    
    /**
     * Crear servicio de notificaciones específico para el rol
     */
    NotificationService createNotificationService();
    
    /**
     * Crear servicio de inventario específico para el rol
     */
    InventoryService createInventoryService();
    
    /**
     * Obtener el tipo de rol que esta fábrica soporta
     */
    String getRoleType();
}

