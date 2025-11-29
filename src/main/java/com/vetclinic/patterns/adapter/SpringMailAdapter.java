package com.vetclinic.patterns.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Spring Mail Adapter
 * Adapts Spring's JavaMailSender to our EmailServiceAdapter interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringMailAdapter implements EmailServiceAdapter {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            // Log detallado para rastrear duplicados
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("🚨🚨🚨 ENVIANDO EMAIL HTML 🚨🚨🚨");
            log.error("   Para: {}", to);
            log.error("   Asunto: {}", subject);
            log.error("   Thread: {}", Thread.currentThread().getName());
            log.error("   Stack trace:");
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (int i = 0; i < Math.min(10, stack.length); i++) {
                log.error("      {} - {}", i, stack[i].toString());
            }
            log.error("═══════════════════════════════════════════════════════════════");
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            // Adjuntar logo como inline attachment (CID)
            try {
                byte[] logoBytes = downloadLogoFromImgur();
                ByteArrayResource logoResource = new ByteArrayResource(logoBytes);
                helper.addInline("logo", logoResource, "image/png");
                log.info("Logo adjuntado exitosamente como inline attachment");
            } catch (Exception e) {
                log.error("Error al adjuntar logo, email se enviará sin él", e);
            }
            
            mailSender.send(mimeMessage);
            log.error("✅✅✅ EMAIL HTML ENVIADO EXITOSAMENTE a: {} ✅✅✅", to);
        } catch (MessagingException e) {
            log.error("❌❌❌ ERROR ENVIANDO EMAIL HTML a: {} ❌❌❌", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    /**
     * Descarga el logo desde Imgur
     */
    private byte[] downloadLogoFromImgur() throws IOException {
        URL url = new URL("https://i.imgur.com/y9qQYK4.png");
        try (InputStream inputStream = url.openStream()) {
            return StreamUtils.copyToByteArray(inputStream);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            
            mailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}
