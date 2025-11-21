package com.vetclinic.repository;

import com.vetclinic.entity.InformedConsent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad InformedConsent
 * RF016 - Gestión de Consentimientos Informados
 */
@Repository
public interface InformedConsentRepository extends JpaRepository<InformedConsent, Long> {

    /**
     * Encontrar consentimientos activos paginados
     */
    Page<InformedConsent> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Encontrar consentimientos por paciente
     */
    List<InformedConsent> findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(Long patientId);

    /**
     * Encontrar consentimientos por propietario
     */
    List<InformedConsent> findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(Long ownerId);

    /**
     * Encontrar consentimientos por veterinario
     */
    List<InformedConsent> findByVeterinarianIdAndIsActiveTrueOrderByCreatedAtDesc(UUID veterinarianId);

    /**
     * Encontrar consentimientos firmados
     */
    List<InformedConsent> findByIsSignedTrueAndIsActiveTrueOrderBySignedDateDesc();

    /**
     * Encontrar consentimientos pendientes de firma
     */
    List<InformedConsent> findByIsSignedFalseAndIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Contar consentimientos activos
     */
    long countByIsActiveTrue();

    /**
     * Contar consentimientos firmados
     */
    long countByIsSignedTrueAndIsActiveTrue();
}



