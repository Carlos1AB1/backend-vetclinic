package com.vetclinic.patterns.abstractfactory;

import com.vetclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Abstract Factory Pattern
 * Fábrica concreta para servicios de veterinario
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VeterinarianServiceFactory implements UserServiceFactory {

    private final AppointmentService appointmentService;

    @Override
    public AppointmentService createAppointmentService() {
        log.debug("Creando servicio de citas para VETERINARIO");
        // En una implementación real, esto podría devolver un wrapper específico
        return appointmentService;
    }

    @Override
    public NotificationService createNotificationService() {
        log.debug("Creando servicio de notificaciones para VETERINARIO");
        return new VeterinarianNotificationService();
    }

    @Override
    public InventoryService createInventoryService() {
        log.debug("Creando servicio de inventario para VETERINARIO");
        return new VeterinarianInventoryService();
    }

    @Override
    public String getRoleType() {
        return "VETERINARIO";
    }

    /**
     * Servicio de notificaciones específico para veterinarios
     */
    private static class VeterinarianNotificationService implements NotificationService {
        @Override
        public void sendNotification(String recipient, String subject, String message) {
            // Lógica específica para veterinarios
            // Por ejemplo, incluir información adicional o usar un canal diferente
        }

        @Override
        public String getServiceType() {
            return "VETERINARIAN_NOTIFICATION";
        }
    }

    /**
     * Servicio de inventario específico para veterinarios
     */
    private static class VeterinarianInventoryService implements InventoryService {
        @Override
        public boolean checkAvailability(String item, int quantity) {
            // Lógica específica para veterinarios
            // Pueden tener acceso completo al inventario
            return true; // Placeholder
        }

        @Override
        public String getServiceType() {
            return "VETERINARIAN_INVENTORY";
        }
    }
}

