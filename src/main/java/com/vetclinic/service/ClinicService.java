package com.vetclinic.service;

import com.vetclinic.dto.service.CreateServiceRequest;
import com.vetclinic.dto.service.ServiceDTO;
import com.vetclinic.dto.service.UpdateServiceRequest;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión del catálogo de servicios
 * RF017 - Catálogo de Servicios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClinicService {

    private final ServiceRepository serviceRepository;

    /**
     * Crear un nuevo servicio
     */
    public ServiceDTO createService(CreateServiceRequest request) {
        log.info("Creando nuevo servicio: {}", request.getName());

        com.vetclinic.entity.Service service = new com.vetclinic.entity.Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setPrice(request.getPrice());
        service.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        service.setRequiresAppointment(request.getRequiresAppointment() != null ? request.getRequiresAppointment() : true);
        service.setIsActive(true);

        com.vetclinic.entity.Service savedService = serviceRepository.save(service);
        log.info("Servicio creado exitosamente con ID: {}", savedService.getId());

        return mapToDTO(savedService);
    }

    /**
     * Obtener servicio por ID
     */
    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(Long id) {
        log.info("Buscando servicio con ID: {}", id);
        com.vetclinic.entity.Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        return mapToDTO(service);
    }

    /**
     * Obtener servicios con paginación
     */
    @Transactional(readOnly = true)
    public Page<ServiceDTO> getServicesPage(Pageable pageable) {
        log.info("Obteniendo página de servicios: {}", pageable);
        return serviceRepository.findByIsActiveTrueOrderByNameAsc(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener servicios por categoría
     */
    @Transactional(readOnly = true)
    public Page<ServiceDTO> getServicesByCategory(String category, Pageable pageable) {
        log.info("Obteniendo servicios por categoría: {}", category);
        return serviceRepository.findByCategoryAndIsActiveTrueOrderByNameAsc(category, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener todos los servicios activos por categoría (sin paginación)
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServicesByCategory(String category) {
        log.info("Obteniendo todos los servicios activos por categoría: {}", category);
        return serviceRepository.findByCategoryAndIsActiveTrueOrderByNameAsc(category)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Buscar servicios por nombre
     */
    @Transactional(readOnly = true)
    public Page<ServiceDTO> searchServices(String search, Pageable pageable) {
        log.info("Buscando servicios: {}", search);
        return serviceRepository.searchServices(search, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Obtener todos los servicios activos
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllActiveServices() {
        log.info("Obteniendo todos los servicios activos");
        return serviceRepository.findByIsActiveTrueOrderByNameAsc(Pageable.unpaged())
            .getContent()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar servicio
     */
    public ServiceDTO updateService(Long id, UpdateServiceRequest request) {
        log.info("Actualizando servicio con ID: {}", id);

        com.vetclinic.entity.Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        // Actualizar campos
        if (request.getName() != null) service.setName(request.getName());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getCategory() != null) service.setCategory(request.getCategory());
        if (request.getPrice() != null) service.setPrice(request.getPrice());
        if (request.getDurationMinutes() != null) service.setDurationMinutes(request.getDurationMinutes());
        if (request.getRequiresAppointment() != null) service.setRequiresAppointment(request.getRequiresAppointment());
        if (request.getIsActive() != null) service.setIsActive(request.getIsActive());

        com.vetclinic.entity.Service updatedService = serviceRepository.save(service);
        log.info("Servicio actualizado exitosamente");

        return mapToDTO(updatedService);
    }

    /**
     * Eliminar servicio (soft delete)
     */
    public void deleteService(Long id) {
        log.info("Eliminando servicio con ID: {}", id);

        com.vetclinic.entity.Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        service.setIsActive(false);
        serviceRepository.save(service);

        log.info("Servicio eliminado exitosamente");
    }

    /**
     * Contar servicios activos
     */
    @Transactional(readOnly = true)
    public long countActiveServices() {
        return serviceRepository.countByIsActiveTrue();
    }

    /**
     * Mapear entidad a DTO
     */
    private ServiceDTO mapToDTO(com.vetclinic.entity.Service service) {
        return ServiceDTO.builder()
            .id(service.getId())
            .name(service.getName())
            .description(service.getDescription())
            .category(service.getCategory())
            .price(service.getPrice())
            .durationMinutes(service.getDurationMinutes())
            .requiresAppointment(service.getRequiresAppointment())
            .isActive(service.getIsActive())
            .createdAt(service.getCreatedAt())
            .updatedAt(service.getUpdatedAt())
            .build();
    }
}

