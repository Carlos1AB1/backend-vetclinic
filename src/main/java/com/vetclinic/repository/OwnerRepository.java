package com.vetclinic.repository;

import com.vetclinic.entity.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Owner
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    /**
     * Buscar propietario por email
     */
    Optional<Owner> findByEmailAndIsActiveTrue(String email);

    /**
     * Buscar propietario por número de documento
     */
    Optional<Owner> findByDocumentNumberAndIsActiveTrue(String documentNumber);

    /**
     * Verificar si existe un email
     */
    boolean existsByEmailAndIsActiveTrue(String email);

    /**
     * Verificar si existe un número de documento
     */
    boolean existsByDocumentNumberAndIsActiveTrue(String documentNumber);

    /**
     * Obtener todos los propietarios activos
     */
    List<Owner> findAllByIsActiveTrueOrderByLastNameAsc();

    /**
     * Obtener propietarios activos con paginación
     */
    Page<Owner> findAllByIsActiveTrue(Pageable pageable);

    /**
     * Buscar propietarios por nombre o apellido
     */
    @Query("SELECT o FROM Owner o WHERE o.isActive = true AND " +
           "(LOWER(o.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Owner> searchOwners(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Buscar propietarios por ciudad
     */
    List<Owner> findByCityAndIsActiveTrueOrderByLastNameAsc(String city);

    /**
     * Contar propietarios activos
     */
    long countByIsActiveTrue();
}
