package com.vetclinic.patterns.observer;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Owner;
import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import com.vetclinic.patterns.adapter.SmsServiceAdapter;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.service.EmailTemplateService;
import com.vetclinic.service.SmsTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final SmsServiceAdapter smsServiceAdapter;
    private final EmailTemplateService emailTemplateService;
    private final SmsTemplateService smsTemplateService;
    private final AppointmentRepository appointmentRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
    
    // Set para evitar envíos duplicados (protección adicional) - STATIC para compartir entre todas las instancias
    private static final Set<String> processedEvents = ConcurrentHashMap.newKeySet();
    
    // Contador para debugging
    private static final AtomicInteger eventCounter = new AtomicInteger(0);

    @EventListener
    @Async
    @Transactional(readOnly = true)
    public void handleAppointmentEvent(AppointmentEvent event) {
        Appointment appointment = event.getAppointment();
        AppointmentEvent.AppointmentEventType eventType = event.getEventType();
        
        // IMPORTANTE: Recargar la cita con relaciones para evitar LazyInitializationException
        // ya que @Async se ejecuta fuera de la transacción original
        log.error("🔄 Recargando cita con relaciones desde BD...");
        Appointment reloadedAppointment = appointmentRepository.findById(appointment.getId())
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + appointment.getId()));
        
        // Forzar carga de relaciones dentro de esta transacción
        if (reloadedAppointment.getOwner() != null) {
            reloadedAppointment.getOwner().getEmail(); // Forzar carga
        }
        if (reloadedAppointment.getPatient() != null) {
            reloadedAppointment.getPatient().getName(); // Forzar carga
        }
        if (reloadedAppointment.getVeterinarian() != null) {
            reloadedAppointment.getVeterinarian().getFirstName(); // Forzar carga
        }
        
        log.error("✅ Cita recargada con relaciones");
        
        // Crear clave única para este evento (cita ID + tipo de evento + timestamp aproximado)
        String eventKey = reloadedAppointment.getId() + "_" + eventType.name();
        int counter = eventCounter.incrementAndGet();

        log.error("═══════════════════════════════════════════════════════════════");
        log.error("📧 OBSERVER RECIBIÓ EVENTO #{}", counter);
        log.error("   Tipo: {}", eventType);
        log.error("   Cita ID: {}", reloadedAppointment.getId());
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
            log.error("🔄 Procesando evento tipo: {}", eventType);
            switch (eventType) {
                case CREATED -> sendAppointmentCreatedNotification(reloadedAppointment);
                case CONFIRMED -> sendAppointmentConfirmedNotification(reloadedAppointment);
                case CANCELLED -> {
                    log.error("🎯 Ejecutando sendAppointmentCancelledNotification...");
                    sendAppointmentCancelledNotification(reloadedAppointment);
                    log.error("✅ sendAppointmentCancelledNotification completado");
                }
                case COMPLETED -> sendAppointmentCompletedNotification(reloadedAppointment);
                case STATUS_CHANGED -> sendAppointmentStatusChangedNotification(reloadedAppointment, event.getPreviousStatus());
            }
            log.error("✅✅✅ Evento procesado exitosamente ✅✅✅");
        } catch (Exception e) {
            log.error("❌❌❌ ERROR AL ENVIAR NOTIFICACIÓN ❌❌❌");
            log.error("   Cita ID: {}", reloadedAppointment.getId());
            log.error("   Tipo de evento: {}", eventType);
            log.error("   Error: {}", e.getMessage());
            log.error("   Stack trace completo:", e);
            // Si hay error, remover de processed para permitir reintento
            processedEvents.remove(eventKey);
            log.error("═══════════════════════════════════════════════════════════════");
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
        
        // Enviar SMS
        if (owner != null && owner.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getAppointmentCreatedSms(
                    owner.getFullName(),
                    appointment.getPatient().getName(),
                    appointment.getScheduledDate().format(DATE_FORMATTER),
                    appointment.getAppointmentType()
                );
                smsServiceAdapter.sendSms(owner.getPhone(), smsMessage);
                log.info("✅ SMS de cita creada enviado a: {}", owner.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de cita creada a: {}", owner.getPhone(), e);
            }
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
        
        // Enviar SMS
        if (owner != null && owner.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getAppointmentConfirmedSms(
                    owner.getFullName(),
                    appointment.getPatient().getName(),
                    appointment.getScheduledDate().format(DATE_FORMATTER)
                );
                smsServiceAdapter.sendSms(owner.getPhone(), smsMessage);
                log.info("✅ SMS de cita confirmada enviado a: {}", owner.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de cita confirmada a: {}", owner.getPhone(), e);
            }
        }
    }

    private void sendAppointmentCancelledNotification(Appointment appointment) {
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.error("📨 ENVIANDO CORREO DE CITA CANCELADA");
        log.error("   Cita ID: {}", appointment.getId());
        log.error("   Thread: {}", Thread.currentThread().getName());
        
        Owner owner = appointment.getOwner();
        log.error("   Owner: {}", owner != null ? owner.getFullName() : "NULL");
        log.error("   Owner email: {}", owner != null ? owner.getEmail() : "NULL");
        
        if (owner == null) {
            log.error("❌ ERROR: Owner es NULL para cita ID: {}", appointment.getId());
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        
        if (owner.getEmail() == null || owner.getEmail().trim().isEmpty()) {
            log.error("❌ ERROR: Email del owner es NULL o vacío para cita ID: {}", appointment.getId());
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        
        try {
            log.error("   ✅ Owner y email válidos - Generando template...");
            String subject = "Cita Cancelada - VetClinic Pro";
            
            String patientName = appointment.getPatient() != null ? appointment.getPatient().getName() : "N/A";
            String scheduledDate = appointment.getScheduledDate() != null ? 
                appointment.getScheduledDate().format(DATE_FORMATTER) : "N/A";
            String appointmentType = appointment.getAppointmentType() != null ? 
                appointment.getAppointmentType() : "N/A";
            
            log.error("   Datos para template:");
            log.error("      - Owner: {}", owner.getFullName());
            log.error("      - Patient: {}", patientName);
            log.error("      - Date: {}", scheduledDate);
            log.error("      - Type: {}", appointmentType);
            
            String htmlBody = emailTemplateService.getAppointmentCancelledEmailTemplate(
                owner.getFullName(),
                patientName,
                scheduledDate,
                appointmentType
            );
            
            log.error("   ✅ Template generado - Llamando a sendHtmlEmail...");
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.error("✅✅✅ CORREO DE CANCELACIÓN ENVIADO EXITOSAMENTE a: {} - Cita ID: {} ✅✅✅", owner.getEmail(), appointment.getId());
        } catch (Exception e) {
            log.error("❌❌❌ ERROR AL ENVIAR CORREO DE CANCELACIÓN ❌❌❌");
            log.error("   Cita ID: {}", appointment.getId());
            log.error("   Email destino: {}", owner.getEmail());
            log.error("   Error: {}", e.getMessage());
            log.error("   Stack trace:", e);
            throw e; // Re-lanzar para que se capture en el catch superior
        }
        
        // Enviar SMS
        if (owner.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String patientName = appointment.getPatient() != null ? appointment.getPatient().getName() : "N/A";
                String scheduledDate = appointment.getScheduledDate() != null ? 
                    appointment.getScheduledDate().format(DATE_FORMATTER) : "N/A";
                String smsMessage = smsTemplateService.getAppointmentCancelledSms(
                    owner.getFullName(),
                    patientName,
                    scheduledDate
                );
                smsServiceAdapter.sendSms(owner.getPhone(), smsMessage);
                log.info("✅ SMS de cita cancelada enviado a: {}", owner.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de cita cancelada a: {}", owner.getPhone(), e);
        }
        }
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
        
        // Enviar SMS
        if (owner != null && owner.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getAppointmentCompletedSms(
                    owner.getFullName(),
                    appointment.getPatient().getName()
                );
                smsServiceAdapter.sendSms(owner.getPhone(), smsMessage);
                log.info("✅ SMS de cita completada enviado a: {}", owner.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de cita completada a: {}", owner.getPhone(), e);
            }
        }
    }

    private void sendAppointmentStatusChangedNotification(Appointment appointment, String previousStatus) {
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.error("📨 ENVIANDO CORREO DE CAMBIO DE ESTADO");
        log.error("   Cita ID: {}", appointment.getId());
        log.error("   Estado anterior: {}", previousStatus);
        log.error("   Estado actual: {}", appointment.getStatus());
        log.error("   Thread: {}", Thread.currentThread().getName());
        
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            log.error("   Email destino: {}", owner.getEmail());
            String subject = "Cambio de Estado de Cita - VetClinic Pro";
            String htmlBody = emailTemplateService.getAppointmentStatusChangedEmailTemplate(
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate().format(DATE_FORMATTER),
                previousStatus,
                appointment.getStatus().toString()
            );
            emailServiceAdapter.sendHtmlEmail(owner.getEmail(), subject, htmlBody);
            log.error("✅✅✅ CORREO DE CAMBIO DE ESTADO ENVIADO EXITOSAMENTE a: {} - Cita ID: {} ✅✅✅", owner.getEmail(), appointment.getId());
        } else {
            log.error("⚠ No se puede enviar correo de cambio de estado - Owner o email es null para cita ID: {}", appointment.getId());
        }
        
        // Enviar SMS
        if (owner != null && owner.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getAppointmentStatusChangedSms(
                    owner.getFullName(),
                    appointment.getPatient().getName(),
                    appointment.getScheduledDate().format(DATE_FORMATTER),
                    previousStatus,
                    appointment.getStatus().toString()
                );
                smsServiceAdapter.sendSms(owner.getPhone(), smsMessage);
                log.info("✅ SMS de cambio de estado enviado a: {}", owner.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de cambio de estado a: {}", owner.getPhone(), e);
        }
        }
        
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}

