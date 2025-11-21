package com.vetclinic.patterns.abstractfactory;

/**
 * Abstract Factory Pattern
 * Interfaz para servicios de notificación específicos por rol
 */
public interface NotificationService {
    
    void sendNotification(String recipient, String subject, String message);
    
    String getServiceType();
}

