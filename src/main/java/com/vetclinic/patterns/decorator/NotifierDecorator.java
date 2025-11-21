package com.vetclinic.patterns.decorator;

import lombok.RequiredArgsConstructor;

/**
 * Decorator Pattern
 * Clase base abstracta para decoradores de notificadores
 */
@RequiredArgsConstructor
public abstract class NotifierDecorator implements Notifier {
    
    protected final Notifier notifier;

    @Override
    public void send(String recipient, String subject, String message) {
        notifier.send(recipient, subject, message);
    }
}

