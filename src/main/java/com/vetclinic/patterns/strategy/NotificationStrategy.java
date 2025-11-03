package com.vetclinic.patterns.strategy;

/**
 * Strategy Pattern - Notification Strategy
 * Interface for different notification channels
 */
public interface NotificationStrategy {
    
    void send(String recipient, String subject, String message);
    
    String getChannelType();
    
    boolean isAvailable();
}
