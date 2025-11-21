package com.vetclinic.patterns.decorator;

/**
 * Decorator Pattern
 * Interfaz base para notificadores
 */
public interface Notifier {
    
    /**
     * Enviar notificación
     * 
     * @param recipient Destinatario
     * @param subject Asunto
     * @param message Mensaje
     */
    void send(String recipient, String subject, String message);
}

