package com.vetclinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for generating SMS message templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsTemplateService {

    @Value("${app.clinic-name:VetClinic Pro}")
    private String clinicName;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
    private static final int MAX_SMS_LENGTH = 160; // Standard SMS length

    // --- APPOINTMENT SMS TEMPLATES ---

    public String getAppointmentCreatedSms(String ownerName, String patientName, String scheduledDate, String appointmentType) {
        String message = String.format(
            "Hola %s, tu cita para %s ha sido creada el %s. Tipo: %s. - %s",
            ownerName, patientName, scheduledDate, appointmentType, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getAppointmentConfirmedSms(String ownerName, String patientName, String scheduledDate) {
        String message = String.format(
            "Hola %s, tu cita para %s ha sido confirmada para el %s. - %s",
            ownerName, patientName, scheduledDate, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getAppointmentCancelledSms(String ownerName, String patientName, String scheduledDate) {
        String message = String.format(
            "Hola %s, tu cita para %s del %s ha sido cancelada. Puedes reagendar en nuestro sitio web. - %s",
            ownerName, patientName, scheduledDate, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getAppointmentCompletedSms(String ownerName, String patientName) {
        String message = String.format(
            "Hola %s, la cita de %s ha sido completada. Gracias por confiar en nosotros. - %s",
            ownerName, patientName, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getAppointmentStatusChangedSms(String ownerName, String patientName, String scheduledDate, String previousStatus, String newStatus) {
        String message = String.format(
            "Hola %s, el estado de la cita de %s del %s cambió de %s a %s. - %s",
            ownerName, patientName, scheduledDate, previousStatus, newStatus, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getAppointmentReminderSms(String ownerName, String patientName, String scheduledDate, int hoursBefore) {
        String message = String.format(
            "Recordatorio: Tu cita para %s es el %s (%d horas). - %s",
            patientName, scheduledDate, hoursBefore, clinicName
        );
        return truncateIfNeeded(message);
    }

    // --- AUTHENTICATION SMS TEMPLATES ---

    public String getPasswordResetSms(String ownerName, String resetLink) {
        // SMS doesn't support links well, so we'll send a token or code instead
        String message = String.format(
            "Hola %s, solicitaste restablecer tu contraseña. Visita nuestro sitio para continuar. - %s",
            ownerName, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getPasswordChangedSms(String ownerName) {
        String message = String.format(
            "Hola %s, tu contraseña ha sido actualizada exitosamente. - %s",
            ownerName, clinicName
        );
        return truncateIfNeeded(message);
    }

    public String getWelcomeSms(String ownerName) {
        String message = String.format(
            "Bienvenido a %s, %s. Tu cuenta ha sido creada exitosamente.",
            clinicName, ownerName
        );
        return truncateIfNeeded(message);
    }

    // --- HELPER METHODS ---

    /**
     * Truncate message if it exceeds SMS length limit
     */
    private String truncateIfNeeded(String message) {
        if (message.length() > MAX_SMS_LENGTH) {
            String truncated = message.substring(0, MAX_SMS_LENGTH - 3) + "...";
            log.warn("SMS message truncated from {} to {} characters", message.length(), truncated.length());
            return truncated;
        }
        return message;
    }
}

