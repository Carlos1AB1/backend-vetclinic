package com.vetclinic.patterns.adapter;

/**
 * Adapter Pattern - Email Service Adapter
 * Interface for email service implementations
 */
public interface EmailServiceAdapter {

    /**
     * Envía un correo electrónico de texto plano.
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Envía un correo electrónico con contenido HTML.
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Envía un correo electrónico con archivo adjunto.
     */
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath);
}
