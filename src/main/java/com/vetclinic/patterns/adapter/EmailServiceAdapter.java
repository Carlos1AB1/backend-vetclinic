package com.vetclinic.patterns.adapter;

/**
 * Adapter Pattern - Email Service Adapter
 * Interface for email service implementations
 */
public interface EmailServiceAdapter {
    
    void sendEmail(String to, String subject, String body);
    
    void sendHtmlEmail(String to, String subject, String htmlBody);
    
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath);
}
