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
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("📧 SPRING MAIL ADAPTER: Enviando email HTML");
            log.error("   Destino: {}", to);
            log.error("   Asunto: {}", subject);
            log.error("   Thread: {}", Thread.currentThread().getName());
            log.error("═══════════════════════════════════════════════════════════════");
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Reemplazar cualquier referencia cid:logo con la URL directa de Imgur
            String processedHtml = htmlBody.replace("cid:logo", "https://i.imgur.com/y9qQYK4.png");
            helper.setText(processedHtml, true);
            
            log.error("📤 Enviando mensaje MIME...");
            mailSender.send(mimeMessage);
            
            log.error("✅✅✅ EMAIL HTML ENVIADO EXITOSAMENTE a: {} ✅✅✅", to);
            log.error("═══════════════════════════════════════════════════════════════");
        } catch (MessagingException e) {
            log.error("❌❌❌ ERROR ENVIANDO EMAIL HTML ❌❌❌");
            log.error("   Destino: {}", to);
            log.error("   Error: {}", e.getMessage());
            log.error("   Stack trace completo:", e);
            log.error("═══════════════════════════════════════════════════════════════");
            throw new RuntimeException("Failed to send HTML email", e);
        } catch (Exception e) {
            log.error("❌❌❌ ERROR INESPERADO EN ENVÍO DE EMAIL ❌❌❌");
            log.error("   Destino: {}", to);
            log.error("   Error: {}", e.getMessage());
            log.error("   Stack trace completo:", e);
            log.error("═══════════════════════════════════════════════════════════════");
            throw new RuntimeException("Failed to send HTML email", e);
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
