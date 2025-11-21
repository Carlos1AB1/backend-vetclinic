package com.vetclinic.patterns.abstractfactory;

import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Abstract Factory Pattern
 * Fábrica concreta para servicios de recepcionista
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReceptionistServiceFactory implements UserServiceFactory {

    private final AppointmentService appointmentService;

    @Override
    public AppointmentService createAppointmentService() {
        log.debug("Creando servicio de citas para RECEPCIONISTA");
        // En una implementación real, esto podría devolver un wrapper específico
        return appointmentService;
    }

    @Override
    public NotificationService createNotificationService() {
        log.debug("Creando servicio de notificaciones para RECEPCIONISTA");
        return new ReceptionistNotificationService();
    }

    @Override
    public InventoryService createInventoryService() {
        log.debug("Creando servicio de inventario para RECEPCIONISTA");
        return new ReceptionistInventoryService();
    }

    @Override
    public String getRoleType() {
        return "RECEPCIONISTA";
    }

    /**
     * Servicio de notificaciones específico para recepcionistas
     */
    private static class ReceptionistNotificationService implements NotificationService {
        @Override
        public void sendNotification(String recipient, String subject, String message) {
            // Lógica específica para recepcionistas
            // Pueden enviar notificaciones estándar a clientes
        }

        @Override
        public String getServiceType() {
            return "RECEPTIONIST_NOTIFICATION";
        }
    }

    /**
     * Servicio de inventario específico para recepcionistas
     */
    private static class ReceptionistInventoryService implements InventoryService {
        @Override
        public boolean checkAvailability(String item, int quantity) {
            // Lógica específica para recepcionistas
            // Pueden consultar pero no modificar inventario
            return true; // Placeholder
        }

        @Override
        public String getServiceType() {
            return "RECEPTIONIST_INVENTORY";
        }
    }
}

