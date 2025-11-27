package com.vetclinic.repository;

import com.vetclinic.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de pacientes
 * Implementa patrón Repository de Spring Data JPA
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Buscar pacientes activos
     */
    List<Patient> findByIsActiveTrue();

    /**
     * Buscar pacientes activos con propietario cargado (para reportes)
     */
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.owner WHERE p.isActive = true ORDER BY p.name ASC")
    List<Patient> findByIsActiveTrueWithOwner();

    /**
     * Buscar paciente por número de microchip
     */
    Optional<Patient> findByMicrochipNumber(String microchipNumber);

    /**
     * Buscar pacientes por propietario
     */
    List<Patient> findByOwnerId(Long ownerId);
    List<Patient> findByOwnerIdAndIsActiveTrue(Long ownerId);

    /**
     * Buscar pacientes por especie
     */
    List<Patient> findBySpecies(String species);

    /**
     * Buscar pacientes por nombre (búsqueda parcial)
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isActive = true")
    List<Patient> searchByName(@Param("name") String name);

    /**
     * Buscar pacientes con paginación
     */
    Page<Patient> findByIsActiveTrue(Pageable pageable);

    /**
     * Contar pacientes activos
     */
    long countByIsActiveTrue();

    /**
     * Verificar si existe microchip
     */
    boolean existsByMicrochipNumber(String microchipNumber);
}
