package com.vetclinic.service;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.AppointmentActionToken;
import com.vetclinic.entity.AppointmentActionToken.ActionType;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.exception.TokenExpiredException;
import com.vetclinic.repository.AppointmentActionTokenRepository;
import com.vetclinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Servicio para gestionar tokens de acciones de citas
 * Permite generar y validar tokens para confirmar, cancelar o reprogramar citas desde recordatorios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentActionTokenService {

    private final AppointmentActionTokenRepository tokenRepository;
    private final AppointmentRepository appointmentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.appointment-action-token-expiration:172800000}") // 48 horas por defecto
    private long tokenExpirationMs;

    @Value("${app.backend-url:http://localhost:8081}")
    private String backendUrl;

    @Value("${server.servlet.context-path:/api}")
    private String apiContextPath;

    /**
     * Generar token para una acción específica de una cita
     */
    public AppointmentActionToken generateToken(Long appointmentId, ActionType actionType) {
        log.info("Generando token para cita ID: {}, acción: {}", appointmentId, actionType);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));

        // Invalidar tokens anteriores del mismo tipo para esta cita
        tokenRepository.findByAppointmentAndActionTypeAndUsedAtIsNull(appointment, actionType)
                .ifPresent(token -> {
                    token.setUsedAt(LocalDateTime.now());
                    tokenRepository.save(token);
                });

        // Generar nuevo token seguro
        String token = generateSecureToken();

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenExpirationMs / 1000);

        AppointmentActionToken actionToken = AppointmentActionToken.builder()
                .token(token)
                .appointment(appointment)
                .actionType(actionType)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        AppointmentActionToken savedToken = tokenRepository.save(actionToken);
        log.info("Token generado exitosamente: {}", savedToken.getId());

        return savedToken;
    }

    /**
     * Validar y obtener token
     */
    @Transactional(readOnly = true)
    public AppointmentActionToken validateToken(String token) {
        log.debug("Validando token: {}", token);

        AppointmentActionToken actionToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido"));

        if (actionToken.isExpired()) {
            throw new TokenExpiredException("El token ha expirado");
        }

        if (actionToken.isUsed()) {
            throw new BusinessException("Este token ya ha sido utilizado");
        }

        return actionToken;
    }

    /**
     * Marcar token como usado
     */
    public void markTokenAsUsed(String token) {
        log.info("Marcando token como usado: {}", token);
        AppointmentActionToken actionToken = validateToken(token);
        actionToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(actionToken);
    }

    /**
     * Generar URL para acción desde recordatorio
     * Genera URL completa del backend API para que el usuario pueda hacer clic directamente
     */
    public String generateActionUrl(Long appointmentId, ActionType actionType) {
        AppointmentActionToken token = generateToken(appointmentId, actionType);
        
        // Determinar el endpoint según el tipo de acción
        String endpoint;
        switch (actionType) {
            case CONFIRM:
                endpoint = String.format("%s/appointments/%d/confirm?token=%s", 
                        apiContextPath, appointmentId, token.getToken());
                break;
            case CANCEL:
                endpoint = String.format("%s/appointments/%d/cancel-reminder?token=%s", 
                        apiContextPath, appointmentId, token.getToken());
                break;
            case RESCHEDULE:
                endpoint = String.format("%s/appointments/%d/reschedule?token=%s", 
                        apiContextPath, appointmentId, token.getToken());
                break;
            default:
                throw new IllegalArgumentException("Tipo de acción no válido: " + actionType);
        }
        
        // Retornar URL completa del backend
        String baseUrl = backendUrl.replaceAll("/$", ""); // Remover trailing slash si existe
        return String.format("%s%s", baseUrl, endpoint);
    }

    /**
     * Generar token seguro aleatorio
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Limpiar tokens expirados (ejecutar diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM diariamente
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Limpiando tokens de acciones de citas expirados");
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredTokens(now);
        log.info("Limpieza de tokens completada");
    }
}

