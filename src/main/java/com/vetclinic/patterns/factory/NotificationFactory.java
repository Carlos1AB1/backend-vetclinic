package com.vetclinic.patterns.factory;

import com.vetclinic.patterns.strategy.NotificationStrategy;
import com.vetclinic.patterns.strategy.EmailNotificationStrategy;
import com.vetclinic.patterns.strategy.SmsNotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern
 * Factory para crear diferentes tipos de notificaciones según el canal requerido
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFactory {

    private final EmailNotificationStrategy emailStrategy;
    private final SmsNotificationStrategy smsStrategy;

    /**
     * Crear una estrategia de notificación según el tipo especificado
     * 
     * @param type Tipo de notificación: EMAIL, SMS
     * @return Estrategia de notificación correspondiente
     * @throws IllegalArgumentException si el tipo no es soportado
     */
    public NotificationStrategy create(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de notificación no puede ser nulo o vacío");
        }

        String normalizedType = type.toUpperCase().trim();

        return switch (normalizedType) {
            case "EMAIL" -> {
                log.debug("Creando estrategia de notificación EMAIL");
                yield emailStrategy;
            }
            case "SMS" -> {
                log.debug("Creando estrategia de notificación SMS");
                yield smsStrategy;
            }
            default -> {
                log.warn("Tipo de notificación no soportado: {}. Usando EMAIL por defecto", type);
                yield emailStrategy;
            }
        };
    }

    /**
     * Crear estrategia de notificación por defecto (EMAIL)
     */
    public NotificationStrategy createDefault() {
        return emailStrategy;
    }
}

