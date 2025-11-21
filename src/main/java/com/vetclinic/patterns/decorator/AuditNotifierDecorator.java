package com.vetclinic.patterns.decorator;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Decorator Pattern
 * Decorador que añade funcionalidad de auditoría a las notificaciones
 */
@Slf4j
public class AuditNotifierDecorator extends NotifierDecorator {

    public AuditNotifierDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String recipient, String subject, String message) {
        // Registrar antes de enviar
        log.info("AUDITORÍA: Registrando envío de notificación a {} - Asunto: {} - Fecha: {}", 
            recipient, subject, LocalDateTime.now());
        
        // Delegar al notificador envuelto
        super.send(recipient, subject, message);
        
        // Registrar después de enviar
        log.info("AUDITORÍA: Notificación enviada exitosamente a {} - Fecha: {}", 
            recipient, LocalDateTime.now());
    }
}

