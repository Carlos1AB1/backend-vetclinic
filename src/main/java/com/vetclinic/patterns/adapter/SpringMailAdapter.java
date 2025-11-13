package com.vetclinic.patterns.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Spring Mail Adapter
 * Adapts Spring's JavaMailSender to our EmailServiceAdapter interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringMailAdapter implements EmailServiceAdapter {

    // Cliente de correo de Spring inyectado automáticamente
    private final JavaMailSender mailSender;

    /**
     * Envía un correo de texto plano
     */
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {

            // Crea un mensaje de correo simple
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            // Envía el mensaje usando el cliente de Spring
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Envía un correo con contenido HTML
     */
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            // Crea un mensaje MIME para soportar contenido HTML
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configura los detalles del mensaje
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Envía el mensaje
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Envía un correo con archivo adjunto
     */
    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            // Crea un mensaje MIME para soportar archivos adjuntos
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configura los detalles del mensaje
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            // Adjunta el archivo al mensaje
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);

            // Envía el mensaje
            mailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}
