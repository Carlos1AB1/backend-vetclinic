package com.vetclinic.service;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.AppointmentActionToken.ActionType;
import com.vetclinic.entity.Owner;
import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import com.vetclinic.patterns.strategy.Reminder24HoursStrategy;
import com.vetclinic.patterns.strategy.Reminder1HourStrategy;
import com.vetclinic.patterns.strategy.ReminderStrategy;
import com.vetclinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de recordatorios automáticos
 * Usa Strategy Pattern para diferentes tipos de recordatorios
 * RF018 - Recordatorio de Citas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailServiceAdapter emailServiceAdapter;
    private final Reminder24HoursStrategy reminder24HoursStrategy;
    private final Reminder1HourStrategy reminder1HourStrategy;
    private final AppointmentActionTokenService tokenService;

    /**
     * Enviar recordatorios automáticos cada hora
     * Verifica citas que necesitan recordatorios según las estrategias configuradas
     */
    @Scheduled(cron = "0 0 * * * ?") // Cada hora
    @Transactional(readOnly = true)
    public void sendScheduledReminders() {
        log.info("Iniciando envío de recordatorios automáticos");

        // Obtener citas próximas (próximas 48 horas)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusHours(48);
        List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointments(now, endDate);

        int remindersSent24h = 0;
        int remindersSent1h = 0;

        for (Appointment appointment : upcomingAppointments) {
            // Verificar recordatorio 24 horas antes
            if (reminder24HoursStrategy.shouldSendReminder(appointment)) {
                sendReminder(appointment, reminder24HoursStrategy);
                remindersSent24h++;
            }

            // Verificar recordatorio 1 hora antes
            if (reminder1HourStrategy.shouldSendReminder(appointment)) {
                sendReminder(appointment, reminder1HourStrategy);
                remindersSent1h++;
            }
        }

        log.info("Recordatorios enviados - 24h: {}, 1h: {}", remindersSent24h, remindersSent1h);
    }

    /**
     * Enviar recordatorio usando la estrategia especificada
     */
    private void sendReminder(Appointment appointment, ReminderStrategy strategy) {
        try {
            Owner owner = appointment.getOwner();
            if (owner == null || owner.getEmail() == null) {
                log.warn("No se puede enviar recordatorio - propietario sin email para cita ID: {}", appointment.getId());
                return;
            }

            String subject = String.format("Recordatorio de Cita - %s horas antes", strategy.getHoursBefore());
            String message = buildReminderMessage(appointment, strategy);

            emailServiceAdapter.sendEmail(owner.getEmail(), subject, message);
            log.info("Recordatorio {} enviado para cita ID: {} a {}", 
                strategy.getReminderType(), appointment.getId(), owner.getEmail());

        } catch (Exception e) {
            log.error("Error al enviar recordatorio para cita ID: {}", appointment.getId(), e);
        }
    }

    /**
     * Construir mensaje de recordatorio con enlaces de acción
     */
    private String buildReminderMessage(Appointment appointment, ReminderStrategy strategy) {
        // Generar URLs de acción con tokens seguros
        String confirmUrl = tokenService.generateActionUrl(appointment.getId(), ActionType.CONFIRM);
        String cancelUrl = tokenService.generateActionUrl(appointment.getId(), ActionType.CANCEL);
        String rescheduleUrl = tokenService.generateActionUrl(appointment.getId(), ActionType.RESCHEDULE);

        return String.format(
            "Estimado/a %s,\n\n" +
            "Este es un recordatorio de su cita programada.\n\n" +
            "Detalles de la cita:\n" +
            "- Paciente: %s\n" +
            "- Fecha y hora: %s\n" +
            "- Tipo: %s\n" +
            "- Veterinario: %s\n\n" +
            "Faltan %d horas para su cita.\n\n" +
            "Puede gestionar su cita directamente desde este email:\n\n" +
            "✓ Confirmar cita: %s\n" +
            "✗ Cancelar cita: %s\n" +
            "↻ Reprogramar cita: %s\n\n" +
            "Nota: Estos enlaces son válidos por 48 horas y solo pueden usarse una vez.\n\n" +
            "Si tiene alguna pregunta, por favor contáctenos.\n\n" +
            "Saludos,\n" +
            "VetClinic Pro",
            appointment.getOwner().getFullName(),
            appointment.getPatient().getName(),
            appointment.getScheduledDate(),
            appointment.getAppointmentType(),
            appointment.getVeterinarian().getFullName(),
            strategy.getHoursBefore(),
            confirmUrl,
            cancelUrl,
            rescheduleUrl
        );
    }

    /**
     * Enviar recordatorio manual para una cita específica
     */
    @Transactional(readOnly = true)
    public void sendManualReminder(Long appointmentId, String reminderType) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new com.vetclinic.exception.ResourceNotFoundException("Cita no encontrada"));

        ReminderStrategy strategy = "24H".equalsIgnoreCase(reminderType) 
            ? reminder24HoursStrategy 
            : reminder1HourStrategy;

        sendReminder(appointment, strategy);
    }
}

