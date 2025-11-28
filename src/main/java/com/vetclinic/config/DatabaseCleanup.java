package com.vetclinic.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database Cleanup Component
 * Removes orphaned records before application startup
 */
@Component
@Order(1) // Ejecutar ANTES de DataInitializer
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanup implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Cleaning up orphaned database records...");
        
        try {
            // Eliminar propietarios inactivos sin usuario asociado o con usuario inactivo
            int deletedInactiveOwners = jdbcTemplate.update(
                "DELETE FROM owners WHERE is_active = FALSE"
            );
            
            if (deletedInactiveOwners > 0) {
                log.warn("Deleted {} inactive owners", deletedInactiveOwners);
            }
            
            // Eliminar usuarios inactivos que no sean de sistema
            jdbcTemplate.update(
                "DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE is_active = FALSE)"
            );
            
            int deletedUsers = jdbcTemplate.update(
                "DELETE FROM users WHERE is_active = FALSE AND username NOT IN ('admin', 'system')"
            );
            
            if (deletedUsers > 0) {
                log.warn("Deleted {} inactive users", deletedUsers);
            }
            
            // Eliminar pacientes sin propietario
            int deletedPatients = jdbcTemplate.update(
                "DELETE FROM patients WHERE owner_id NOT IN (SELECT id FROM owners) OR owner_id IS NULL"
            );
            
            if (deletedPatients > 0) {
                log.warn("Deleted {} orphaned patients without valid owner", deletedPatients);
            }
            
            // Eliminar citas sin paciente o propietario o veterinario
            int deletedAppointments = jdbcTemplate.update(
                "DELETE FROM appointments WHERE " +
                "patient_id NOT IN (SELECT id FROM patients) OR " +
                "owner_id NOT IN (SELECT id FROM owners) OR " +
                "veterinarian_id NOT IN (SELECT id FROM users)"
            );
            
            if (deletedAppointments > 0) {
                log.warn("Deleted {} orphaned appointments", deletedAppointments);
            }
            
            // Eliminar registros médicos sin paciente
            int deletedRecords = jdbcTemplate.update(
                "DELETE FROM medical_records WHERE patient_id NOT IN (SELECT id FROM patients)"
            );
            
            if (deletedRecords > 0) {
                log.warn("Deleted {} orphaned medical records", deletedRecords);
            }
            
            // Eliminar prescripciones sin registro médico
            int deletedPrescriptions = jdbcTemplate.update(
                "DELETE FROM prescriptions WHERE medical_record_id NOT IN (SELECT id FROM medical_records)"
            );
            
            if (deletedPrescriptions > 0) {
                log.warn("Deleted {} orphaned prescriptions", deletedPrescriptions);
            }
            
            log.info("Database cleanup completed successfully");
            
        } catch (Exception e) {
            log.error("Error during database cleanup: {}", e.getMessage());
            // No lanzar excepción para no detener el arranque
        }
    }
}
