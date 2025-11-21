package com.vetclinic.patterns.decorator;

import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Decorator Pattern
 * Implementación base del notificador (envío simple)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BaseNotifier implements Notifier {

    private final EmailServiceAdapter emailServiceAdapter;

    @Override
    public void send(String recipient, String subject, String message) {
        log.info("Enviando notificación base a: {}", recipient);
        emailServiceAdapter.sendEmail(recipient, subject, message);
    }
}

