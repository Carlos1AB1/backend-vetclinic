package com.vetclinic.patterns.adapter;

/**
 * Adapter Pattern - SMS Service Adapter
 * Interface for SMS service implementations
 */
public interface SmsServiceAdapter {
    
    /**
     * Send SMS message
     * @param to Phone number in E.164 format (e.g., +1234567890)
     * @param message SMS message content
     */
    void sendSms(String to, String message);
    
    /**
     * Check if SMS service is available
     * @return true if SMS service is configured and available
     */
    boolean isAvailable();
}

