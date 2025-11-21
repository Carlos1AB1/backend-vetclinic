package com.vetclinic.patterns.decorator;

import com.vetclinic.entity.User;
import com.vetclinic.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Decorator Pattern
 * Decorador que envía una copia de la notificación al veterinario
 */
@Slf4j
public class CopyVeterinarianNotifierDecorator extends NotifierDecorator {

    private final UserRepository userRepository;

    public CopyVeterinarianNotifierDecorator(Notifier notifier, UserRepository userRepository) {
        super(notifier);
        this.userRepository = userRepository;
    }

    @Override
    public void send(String recipient, String subject, String message) {
        // Enviar notificación original
        super.send(recipient, subject, message);
        
        // Enviar copia al veterinario si se proporciona el ID
        // Nota: En una implementación real, el ID del veterinario debería pasarse como parámetro
        // Por ahora, este decorador muestra el concepto
        log.info("Copia de notificación enviada al veterinario (concepto de decorador)");
    }

    /**
     * Enviar con copia al veterinario específico
     */
    public void sendWithVeterinarianCopy(String recipient, String subject, String message, UUID veterinarianId) {
        // Enviar notificación original
        super.send(recipient, subject, message);
        
        // Enviar copia al veterinario
        User veterinarian = userRepository.findById(veterinarianId).orElse(null);
        if (veterinarian != null && veterinarian.getEmail() != null) {
            String copySubject = "[COPIA] " + subject;
            String copyMessage = "Esta es una copia de la notificación enviada al cliente:\n\n" + message;
            super.send(veterinarian.getEmail(), copySubject, copyMessage);
            log.info("Copia enviada al veterinario: {}", veterinarian.getEmail());
        }
    }
}

