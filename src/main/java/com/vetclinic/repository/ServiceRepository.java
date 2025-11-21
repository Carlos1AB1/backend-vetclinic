package com.vetclinic.repository;

import com.vetclinic.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Service
 * RF017 - Catálogo de Servicios
 */
@Repository
public interface ServiceRepository extends JpaRepository<com.vetclinic.entity.Service, Long> {

    /**
     * Encontrar servicios activos paginados
     */
    Page<com.vetclinic.entity.Service> findByIsActiveTrueOrderByNameAsc(Pageable pageable);

    /**
     * Encontrar servicios por categoría
     */
    Page<com.vetclinic.entity.Service> findByCategoryAndIsActiveTrueOrderByNameAsc(String category, Pageable pageable);

    /**
     * Buscar servicios por nombre
     */
    @Query("SELECT s FROM Service s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "AND s.isActive = true ORDER BY s.name ASC")
    Page<com.vetclinic.entity.Service> searchServices(@Param("search") String search, Pageable pageable);

    /**
     * Encontrar servicios activos por categoría
     */
    List<com.vetclinic.entity.Service> findByCategoryAndIsActiveTrueOrderByNameAsc(String category);

    /**
     * Contar servicios activos
     */
    long countByIsActiveTrue();
}

