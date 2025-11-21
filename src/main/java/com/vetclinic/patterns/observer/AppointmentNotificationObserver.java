package com.vetclinic.patterns.observer;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Owner;
import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import com.vetclinic.patterns.decorator.AuditNotifierDecorator;
import com.vetclinic.patterns.decorator.BaseNotifier;
import com.vetclinic.patterns.decorator.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern
 * Observador que envía notificaciones cuando cambia el estado de una cita
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentNotificationObserver {

    private final EmailServiceAdapter emailServiceAdapter;
    private final BaseNotifier baseNotifier;

    /**
     * Crear notificador con decoradores usando Decorator Pattern
     */
    private Notifier createNotifier() {
        // Decorar con auditoría
        return new AuditNotifierDecorator(baseNotifier);
    }

    @EventListener
    @Async
    public void handleAppointmentEvent(AppointmentEvent event) {
        Appointment appointment = event.getAppointment();
        AppointmentEvent.AppointmentEventType eventType = event.getEventType();

        log.info("Procesando evento de cita: {} para cita ID: {}", eventType, appointment.getId());

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
        }
    }

    private void sendAppointmentCreatedNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Creada - VetClinic Pro";
            String body = String.format(
                "Estimado/a %s,\n\n" +
                "Su cita ha sido creada exitosamente.\n\n" +
                "Detalles de la cita:\n" +
                "- Paciente: %s\n" +
                "- Fecha y hora: %s\n" +
                "- Tipo: %s\n" +
                "- Veterinario: %s\n\n" +
                "Recibirá una confirmación próximamente.\n\n" +
                "Saludos,\n" +
                "VetClinic Pro",
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate(),
                appointment.getAppointmentType(),
                appointment.getVeterinarian().getFullName()
            );
            // Usar Decorator Pattern para notificaciones con auditoría
            Notifier notifier = createNotifier();
            notifier.send(owner.getEmail(), subject, body);
        }
    }

    private void sendAppointmentConfirmedNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Confirmada - VetClinic Pro";
            String body = String.format(
                "Estimado/a %s,\n\n" +
                "Su cita ha sido confirmada.\n\n" +
                "Detalles:\n" +
                "- Paciente: %s\n" +
                "- Fecha y hora: %s\n" +
                "- Tipo: %s\n" +
                "- Veterinario: %s\n\n" +
                "Le esperamos en la clínica.\n\n" +
                "Saludos,\n" +
                "VetClinic Pro",
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate(),
                appointment.getAppointmentType(),
                appointment.getVeterinarian().getFullName()
            );
            // Usar Decorator Pattern para notificaciones con auditoría
            Notifier notifier = createNotifier();
            notifier.send(owner.getEmail(), subject, body);
        }
    }

    private void sendAppointmentCancelledNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Cancelada - VetClinic Pro";
            String body = String.format(
                "Estimado/a %s,\n\n" +
                "Su cita ha sido cancelada.\n\n" +
                "Detalles de la cita cancelada:\n" +
                "- Paciente: %s\n" +
                "- Fecha y hora: %s\n" +
                "- Tipo: %s\n\n" +
                "Si desea reagendar, por favor contáctenos.\n\n" +
                "Saludos,\n" +
                "VetClinic Pro",
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate(),
                appointment.getAppointmentType()
            );
            // Usar Decorator Pattern para notificaciones con auditoría
            Notifier notifier = createNotifier();
            notifier.send(owner.getEmail(), subject, body);
        }
    }

    private void sendAppointmentCompletedNotification(Appointment appointment) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cita Completada - VetClinic Pro";
            String body = String.format(
                "Estimado/a %s,\n\n" +
                "Su cita ha sido completada.\n\n" +
                "Detalles:\n" +
                "- Paciente: %s\n" +
                "- Fecha: %s\n" +
                "- Veterinario: %s\n\n" +
                "Gracias por confiar en nosotros.\n\n" +
                "Saludos,\n" +
                "VetClinic Pro",
                owner.getFullName(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate(),
                appointment.getVeterinarian().getFullName()
            );
            // Usar Decorator Pattern para notificaciones con auditoría
            Notifier notifier = createNotifier();
            notifier.send(owner.getEmail(), subject, body);
        }
    }

    private void sendAppointmentStatusChangedNotification(Appointment appointment, String previousStatus) {
        Owner owner = appointment.getOwner();
        if (owner != null && owner.getEmail() != null) {
            String subject = "Cambio de Estado de Cita - VetClinic Pro";
            String body = String.format(
                "Estimado/a %s,\n\n" +
                "El estado de su cita ha cambiado.\n\n" +
                "Estado anterior: %s\n" +
                "Estado actual: %s\n\n" +
                "Detalles:\n" +
                "- Paciente: %s\n" +
                "- Fecha: %s\n\n" +
                "Saludos,\n" +
                "VetClinic Pro",
                owner.getFullName(),
                previousStatus,
                appointment.getStatus(),
                appointment.getPatient().getName(),
                appointment.getScheduledDate()
            );
            // Usar Decorator Pattern para notificaciones con auditoría
            Notifier notifier = createNotifier();
            notifier.send(owner.getEmail(), subject, body);
        }
    }
}

