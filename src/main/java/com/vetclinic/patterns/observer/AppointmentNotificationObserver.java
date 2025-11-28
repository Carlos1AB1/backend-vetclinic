package com.vetclinic.patterns.observer;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Owner;
import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import com.vetclinic.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Observer Pattern
 * Observador que envía notificaciones cuando cambia el estado de una cita
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentNotificationObserver {

    private final EmailServiceAdapter emailServiceAdapter;
    private final EmailTemplateService emailTemplateService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
    
    // Set para evitar envíos duplicados (protección adicional) - STATIC para compartir entre todas las instancias
    private static final Set<String> processedEvents = ConcurrentHashMap.newKeySet();
    
    // Contador para debugging
    private static final AtomicInteger eventCounter = new AtomicInteger(0);

    @EventListener
    @Async
    public void handleAppointmentEvent(AppointmentEvent event) {
        Appointment appointment = event.getAppointment();
        AppointmentEvent.AppointmentEventType eventType = event.getEventType();
        
        // Crear clave única para este evento (cita ID + tipo de evento + timestamp aproximado)
        String eventKey = appointment.getId() + "_" + eventType.name();
        int counter = eventCounter.incrementAndGet();

        log.error("═══════════════════════════════════════════════════════════════");
        log.error("📧 OBSERVER RECIBIÓ EVENTO #{}", counter);
        log.error("   Tipo: {}", eventType);
        log.error("   Cita ID: {}", appointment.getId());
        log.error("   Thread: {}", Thread.currentThread().getName());
        log.error("   Source: {}", event.getSource().getClass().getSimpleName());
        log.error("   Key: {}", eventKey);
        log.error("   Ya procesado?: {}", processedEvents.contains(eventKey));
        log.error("═══════════════════════════════════════════════════════════════");

        // Protección contra duplicados - usar computeIfAbsent para operación atómica
        boolean isNew = processedEvents.add(eventKey);
        
        if (!isNew) {
            log.error("❌❌❌ EVENTO DUPLICADO DETECTADO - Ya se procesó: {} - IGNORANDO ENVÍO ❌❌❌", eventKey);
            return;
        }
        
        log.error("✓✓✓ Evento NUEVO - Marcado como procesado: {} ✓✓✓", eventKey);

        try {
            switch (eventType) {
                case CREATED -> sendAppointmentCreatedNotification(appointment);
                case CONFIRMED -> sendAppointmentConfirmedNotification(appointment);
                case CANCELLED -> sendAppointmentCancelledNotification(appointment);
                case COMPLETED -> sendAppointmentCompletedNotification(appointment);
                case STATUS_CHANGED -> sendAppointmentStatusChangedNotification(appointment, event.getPreviousStatus());
            }
        } catch (Exception e) {
            log.error("Error al enviar notificación para cita ID: {}", appointment.getId(), e);
            // Si hay error, remover de processed para permitir reintento
            processedEvents.remove(eventKey);
        }
    }

    private void sendAppointmentCreatedNotification(Appointment appointment) {
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.error("📨 ENVIANDO CORREO DE CITA CREADA");
        log.error("   Cita ID: {}", appointment.getId());
        log.error("   Thread: {}", Thread.currentThread().getName());
        
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            log.error("   Email destino: {}", owner.getEmail());
            String subject = "Cita Creada - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentCreatedEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getAppointmentType(),
                appointment.getVeterinarian().getFullName()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.error("✅✅✅ CORREO ENVIADO EXITOSAMENTE a: {} - Cita ID: {} ✅✅✅", owner.getEmail(), appointment.getId());
        } else {
            log.error("⚠ No se puede enviar correo - Owner o email es null para cita ID: {}", appointment.getId());
        }
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sendAppointmentConfirmedNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Confirmada - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentConfirmedEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getAppointmentType(),
                appointment.getVeterinarian().getFullName()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.info("Email HTML de cita confirmada enviado a: {}", owner.getEmail());
        }
    }

    private void sendAppointmentCancelledNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Cancelada - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentCancelledEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getAppointmentType()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.info("Email HTML de cita cancelada enviado a: {}", owner.getEmail());
        }
    }

    private void sendAppointmentCompletedNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Completada - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentCompletedEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                appointment.getVeterinarian().getFullName()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.info("Email HTML de cita completada enviado a: {}", owner.getEmail());
        }
    }

    private void sendAppointmentStatusChangedNotification(Appointment appointment, String previousStatus) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cambio de Estado de Cita - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentStatusChangedEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                previousStatus,
                appointment.getStatus().toString()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.info("Email HTML de cambio de estado enviado a: {}", owner.getEmail());
        }
    }
}

