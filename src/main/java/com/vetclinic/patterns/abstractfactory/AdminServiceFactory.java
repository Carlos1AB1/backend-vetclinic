package com.vetclinic.patterns.abstractfactory;

import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Abstract Factory Pattern
 * Fábrica concreta para servicios de administrador
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminServiceFactory implements UserServiceFactory {

    private final AppointmentService appointmentService;

    @Override
    public AppointmentService createAppointmentService() {
        log.debug("Creando servicio de citas para ADMINISTRADOR");
        return appointmentService;
    }

    @Override
    public NotificationService createNotificationService() {
        log.debug("Creando servicio de notificaciones para ADMINISTRADOR");
        return new AdminNotificationService();
    }

    @Override
    public InventoryService createInventoryService() {
        log.debug("Creando servicio de inventario para ADMINISTRADOR");
        return new AdminInventoryService();
    }

    @Override
    public String getRoleType() {
        return "ADMINISTRADOR";
    }

    /**
     * Servicio de notificaciones específico para administradores
     */
    private static class AdminNotificationService implements NotificationService {
        @Override
        public void sendNotification(String recipient, String subject, String message) {
            // Lógica específica para administradores
            // Acceso completo a todas las funcionalidades de notificación
        }

        @Override
        public String getServiceType() {
            return "ADMIN_NOTIFICATION";
        }
    }

    /**
     * Servicio de inventario específico para administradores
     */
    private static class AdminInventoryService implements InventoryService {
        @Override
        public boolean checkAvailability(String item, int quantity) {
            // Lógica específica para administradores
            // Acceso completo al inventario
            return true; // Placeholder
        }

        @Override
        public String getServiceType() {
            return "ADMIN_INVENTORY";
        }
    }
}

