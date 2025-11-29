package com.vetclinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio Premium de Email Templates
 * Carga templates HTML desde archivos independientes
 * Diseño: Clean Corporate / Medical SaaS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final ResourceLoader resourceLoader;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.clinic-name:VetClinic Pro}")
    private String clinicName;

    private static final String TEMPLATES_PATH = "classpath:templates/emails/";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Carga un template HTML desde el classpath y reemplaza las variables
     */
    private String loadTemplate(String templateName, Map<String, String> variables) {
        try {
            Resource resource = resourceLoader.getResource(TEMPLATES_PATH + templateName);
            if (!resource.exists()) {
                log.error("Template no encontrado: {}", templateName);
                return "";
            }

            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return replaceVariables(template, variables);
        } catch (IOException e) {
            log.error("Error al cargar template: {}", templateName, e);
            return "";
        }
    }

    /**
     * Reemplaza variables {{variable}} en el template con los valores proporcionados
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String value = variables.getOrDefault(variableName, "");
            // No escapar HTML si es contenido HTML (como el logo)
            if (variableName.equals("logoBase64") || variableName.equals("content")) {
                matcher.appendReplacement(result, value);
            } else {
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Construye el template completo combinando base.html con el contenido específico
     */
    private String buildEmail(String contentTemplate, Map<String, String> contentVariables, 
                              String title, String preheader) {
        // Cargar variables comunes para el base template
        Map<String, String> baseVariables = new HashMap<>();
        baseVariables.put("title", title);
        baseVariables.put("preheader", preheader);
        baseVariables.put("clinicName", clinicName);
        baseVariables.put("frontendUrl", frontendUrl);
        baseVariables.put("currentYear", String.valueOf(Year.now().getValue()));
        baseVariables.put("content", loadTemplate(contentTemplate, contentVariables));

        return loadTemplate("base.html", baseVariables);
    }

    // --- TEMPLATES DE AUTENTICACIÓN ---

    public String getWelcomeEmailTemplate(String fullName, String username, String email, String loginUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("username", username);
        variables.put("email", email);
        variables.put("loginUrl", loginUrl);

        return buildEmail("welcome.html", variables, 
            "Bienvenido a " + clinicName, "Tu cuenta ha sido creada exitosamente");
    }

    public String getOwnerWelcomeEmailTemplate(String fullName, String username, String password, String loginUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("username", username);
        variables.put("password", password);
        variables.put("loginUrl", loginUrl);

        return buildEmail("owner-welcome.html", variables, 
            "Bienvenido - " + clinicName, "Tus credenciales de acceso");
    }

    public String getPasswordResetEmailTemplate(String fullName, String resetLink) {
        Map<String, String> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("resetLink", resetLink);

        return buildEmail("password-reset.html", variables, 
            "Recuperar Contraseña", "Instrucciones para recuperar tu acceso");
    }

    public String getPasswordChangedEmailTemplate(String fullName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("fullName", fullName);

        return buildEmail("password-changed.html", variables, 
            "Contraseña Actualizada", "Confirmación de cambio de contraseña");
    }

    // --- TEMPLATES DE CITAS ---

    public String getAppointmentCreatedEmailTemplate(String ownerName, String patientName, 
                                                      String scheduledDate, String appointmentType, 
                                                      String veterinarianName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("appointmentType", appointmentType);
        variables.put("veterinarianName", veterinarianName);
        variables.put("frontendUrl", frontendUrl);

        return buildEmail("appointment-created.html", variables, 
            "Cita Creada - " + clinicName, "Nueva cita registrada");
    }

    public String getAppointmentConfirmedEmailTemplate(String ownerName, String patientName, 
                                                       String scheduledDate, String appointmentType, 
                                                       String veterinarianName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("appointmentType", appointmentType);
        variables.put("veterinarianName", veterinarianName);
        variables.put("frontendUrl", frontendUrl);

        return buildEmail("appointment-confirmed.html", variables, 
            "Cita Confirmada - " + clinicName, "Tu cita está confirmada");
    }

    public String getAppointmentCancelledEmailTemplate(String ownerName, String patientName, 
                                                       String scheduledDate, String appointmentType) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("appointmentType", appointmentType);
        variables.put("frontendUrl", frontendUrl);

        return buildEmail("appointment-cancelled.html", variables, 
            "Cita Cancelada - " + clinicName, "Cita cancelada");
    }

    public String getAppointmentCompletedEmailTemplate(String ownerName, String patientName, 
                                                       String scheduledDate, String veterinarianName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("veterinarianName", veterinarianName);
        variables.put("frontendUrl", frontendUrl);
        variables.put("clinicName", clinicName);

        return buildEmail("appointment-completed.html", variables, 
            "Cita Completada - " + clinicName, "Cita finalizada");
    }

    public String getAppointmentStatusChangedEmailTemplate(String ownerName, String patientName, 
                                                           String scheduledDate, String previousStatus, 
                                                           String currentStatus) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("previousStatus", previousStatus);
        variables.put("currentStatus", currentStatus);
        variables.put("frontendUrl", frontendUrl);

        return buildEmail("appointment-status-changed.html", variables, 
            "Estado de Cita Actualizado - " + clinicName, "Cambio de estado");
    }

    public String getAppointmentReminderEmailTemplate(String ownerName, String patientName, 
                                                      String scheduledDate, String appointmentType, 
                                                      String veterinarianName, int hoursBefore,
                                                      String confirmUrl, String cancelUrl, String rescheduleUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ownerName", ownerName);
        variables.put("patientName", patientName);
        variables.put("scheduledDate", scheduledDate);
        variables.put("appointmentType", appointmentType);
        variables.put("veterinarianName", veterinarianName);
        variables.put("hoursBefore", String.valueOf(hoursBefore));
        variables.put("confirmUrl", confirmUrl);
        variables.put("cancelUrl", cancelUrl);
        variables.put("rescheduleUrl", rescheduleUrl);

        return buildEmail("appointment-reminder.html", variables, 
            "Recordatorio de Cita - " + clinicName, "Recordatorio de cita");
    }
}
