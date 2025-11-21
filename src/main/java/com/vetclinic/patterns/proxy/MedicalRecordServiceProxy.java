package com.vetclinic.patterns.proxy;

import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.User;
import com.vetclinic.exception.UnauthorizedException;
import com.vetclinic.repository.MedicalRecordRepository;
import com.vetclinic.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Proxy Pattern
 * Proxy para el servicio de historiales médicos que añade control de acceso y auditoría
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordServiceProxy {

    private final MedicalRecordService realService;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Obtener registro médico con control de acceso
     */
    public MedicalRecordDTO getMedicalRecordById(Long id) {
        log.info("PROXY: Acceso a historial médico ID: {}", id);
        
        // Validar permisos
        validateAccess(id);
        
        // Registrar acceso en auditoría
        logAccess(id, "READ");
        
        // Delegar al servicio real
        return realService.getMedicalRecordById(id);
    }

    /**
     * Actualizar registro médico con control de acceso
     */
    public MedicalRecordDTO updateMedicalRecord(Long id, com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest request) {
        log.info("PROXY: Actualización de historial médico ID: {}", id);
        
        // Validar permisos
        validateAccess(id);
        
        // Registrar acceso en auditoría
        logAccess(id, "UPDATE");
        
        // Delegar al servicio real
        return realService.updateMedicalRecord(id, request);
    }

    /**
     * Eliminar registro médico con control de acceso
     */
    public void deleteMedicalRecord(Long id) {
        log.info("PROXY: Eliminación de historial médico ID: {}", id);
        
        // Validar permisos
        validateAccess(id);
        
        // Registrar acceso en auditoría
        logAccess(id, "DELETE");
        
        // Delegar al servicio real
        realService.deleteMedicalRecord(id);
    }

    /**
     * Validar que el usuario tiene permisos para acceder al historial
     */
    private void validateAccess(Long medicalRecordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        User currentUser = (User) authentication.getPrincipal();
        
        // Obtener el historial médico
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
            .orElseThrow(() -> new com.vetclinic.exception.ResourceNotFoundException("Historial médico no encontrado"));

        // Verificar permisos: solo el veterinario que creó el registro o administradores pueden acceder
        UUID currentUserId = currentUser.getId();
        UUID veterinarianId = medicalRecord.getVeterinarian().getId();
        
        boolean isVeterinarian = currentUserId.equals(veterinarianId);
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isVeterinarian && !isAdmin) {
            log.warn("PROXY: Acceso denegado - Usuario {} intentó acceder al historial médico ID: {}", 
                currentUser.getUsername(), medicalRecordId);
            throw new UnauthorizedException("No tiene permisos para acceder a este historial médico");
        }

        log.debug("PROXY: Acceso autorizado para usuario: {}", currentUser.getUsername());
    }

    /**
     * Registrar acceso en auditoría
     */
    private void logAccess(Long medicalRecordId, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "ANONYMOUS";
        
        log.info("AUDITORÍA - Historial Médico: ID={}, Acción={}, Usuario={}, Fecha={}", 
            medicalRecordId, action, username, LocalDateTime.now());
        
        // En una implementación real, esto se guardaría en una tabla de auditoría
    }
}

