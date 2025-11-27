package com.vetclinic.service;

import com.vetclinic.dto.owner.CreateOwnerRequest;
import com.vetclinic.dto.owner.OwnerDTO;
import com.vetclinic.dto.owner.UpdateOwnerRequest;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BadRequestException;
import com.vetclinic.exception.DuplicateResourceException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.OwnerRepository;
import com.vetclinic.repository.RoleRepository;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de propietarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crear un nuevo propietario con usuario asociado
     */
    public OwnerDTO createOwner(CreateOwnerRequest request) {
        log.info("Creando nuevo propietario: {} {}", request.getFirstName(), request.getLastName());

        // Verificar email duplicado
        if (ownerRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un propietario con el email: " + request.getEmail());
        }

        // Verificar username duplicado
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }

        // Verificar email de usuario duplicado
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado como usuario");
        }

        // Verificar documento duplicado si se proporciona
        if (request.getDocumentNumber() != null && !request.getDocumentNumber().isEmpty()) {
            if (ownerRepository.existsByDocumentNumberAndIsActiveTrue(request.getDocumentNumber())) {
                throw new DuplicateResourceException("Ya existe un propietario con el número de documento: " + request.getDocumentNumber());
            }
        }

        // Crear usuario con rol OWNER
        Role ownerRole = roleRepository.findByName("OWNER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "OWNER"));

        Set<Role> roles = new HashSet<>();
        roles.add(ownerRole);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());

        // Crear propietario
        Owner owner = new Owner();
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setEmail(request.getEmail());
        owner.setPhone(request.getPhone());
        owner.setAlternativePhone(request.getAlternativePhone());
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setPostalCode(request.getPostalCode());
        owner.setDocumentType(request.getDocumentType());
        owner.setDocumentNumber(request.getDocumentNumber());
        owner.setNotes(request.getNotes());
        owner.setUserId(savedUser.getId());
        owner.setIsActive(true);

        Owner savedOwner = ownerRepository.save(owner);
        log.info("Propietario creado exitosamente con ID: {}", savedOwner.getId());

        return mapToDTO(savedOwner, savedUser);
    }

    /**
     * Obtener todos los propietarios activos
     */
    @Transactional(readOnly = true)
    public List<OwnerDTO> getAllOwners() {
        log.info("Obteniendo lista de todos los propietarios activos");
        return ownerRepository.findAllByIsActiveTrueOrderByLastNameAsc()
                .stream()
                .map(owner -> {
                    User user = owner.getUserId() != null ?
                            userRepository.findById(owner.getUserId()).orElse(null) : null;
                    return mapToDTO(owner, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtener propietarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<OwnerDTO> getOwnersPage(Pageable pageable) {
        log.info("Obteniendo página de propietarios: {}", pageable);
        return ownerRepository.findAllByIsActiveTrue(pageable)
                .map(owner -> {
                    User user = owner.getUserId() != null ?
                            userRepository.findById(owner.getUserId()).orElse(null) : null;
                    return mapToDTO(owner, user);
                });
    }

    /**
     * Obtener propietario por ID
     */
    @Transactional(readOnly = true)
    public OwnerDTO getOwnerById(Long id) {
        log.info("Buscando propietario con ID: {}", id);
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + id));

        if (!owner.getIsActive()) {
            throw new ResourceNotFoundException("Propietario no encontrado con ID: " + id);
        }

        User user = owner.getUserId() != null ?
                userRepository.findById(owner.getUserId()).orElse(null) : null;

        return mapToDTO(owner, user);
    }

    /**
     * Obtener propietario por User ID
     */
    @Transactional(readOnly = true)
    public Owner getOwnerByUserId(String userId) {
        log.info("Buscando propietario con User ID: {}", userId);
        return ownerRepository.findByUserIdAndIsActiveTrue(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado para el usuario"));
    }

    /**
     * Buscar propietarios
     */
    @Transactional(readOnly = true)
    public Page<OwnerDTO> searchOwners(String searchTerm, Pageable pageable) {
        log.info("Buscando propietarios con término: {}", searchTerm);
        return ownerRepository.searchOwners(searchTerm, pageable)
                .map(owner -> {
                    User user = owner.getUserId() != null ?
                            userRepository.findById(owner.getUserId()).orElse(null) : null;
                    return mapToDTO(owner, user);
                });
    }

    /**
     * Actualizar propietario
     */
    public OwnerDTO updateOwner(Long id, UpdateOwnerRequest request) {
        log.info("Actualizando propietario con ID: {}", id);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + id));

        // Verificar email duplicado si cambió
        if (request.getEmail() != null && !request.getEmail().equals(owner.getEmail())) {
            if (ownerRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
                throw new DuplicateResourceException("Ya existe un propietario con el email: " + request.getEmail());
            }
            owner.setEmail(request.getEmail());
        }

        // Verificar documento duplicado si cambió
        if (request.getDocumentNumber() != null && !request.getDocumentNumber().equals(owner.getDocumentNumber())) {
            if (ownerRepository.existsByDocumentNumberAndIsActiveTrue(request.getDocumentNumber())) {
                throw new DuplicateResourceException("Ya existe un propietario con el número de documento: " + request.getDocumentNumber());
            }
            owner.setDocumentNumber(request.getDocumentNumber());
        }

        // Actualizar campos
        if (request.getFirstName() != null) owner.setFirstName(request.getFirstName());
        if (request.getLastName() != null) owner.setLastName(request.getLastName());
        if (request.getPhone() != null) owner.setPhone(request.getPhone());
        if (request.getAlternativePhone() != null) owner.setAlternativePhone(request.getAlternativePhone());
        if (request.getAddress() != null) owner.setAddress(request.getAddress());
        if (request.getCity() != null) owner.setCity(request.getCity());
        if (request.getPostalCode() != null) owner.setPostalCode(request.getPostalCode());
        if (request.getDocumentType() != null) owner.setDocumentType(request.getDocumentType());
        if (request.getNotes() != null) owner.setNotes(request.getNotes());
        if (request.getIsActive() != null) owner.setIsActive(request.getIsActive());

        Owner updatedOwner = ownerRepository.save(owner);
        log.info("Propietario actualizado exitosamente");

        User user = owner.getUserId() != null ?
                userRepository.findById(owner.getUserId()).orElse(null) : null;

        return mapToDTO(updatedOwner, user);
    }

    /**
     * Eliminar propietario (soft delete)
     */
    public void deleteOwner(Long id) {
        log.info("Eliminando propietario con ID: {}", id);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + id));

        owner.setIsActive(false);
        ownerRepository.save(owner);

        // Desactivar usuario asociado
        if (owner.getUserId() != null) {
            userRepository.findById(owner.getUserId()).ifPresent(user -> {
                user.setIsActive(false);
                userRepository.save(user);
            });
        }

        log.info("Propietario eliminado exitosamente");
    }

    /**
     * Contar propietarios activos
     */
    @Transactional(readOnly = true)
    public long countActiveOwners() {
        return ownerRepository.countByIsActiveTrue();
    }

    /**
     * Obtener propietarios por ciudad
     */
    @Transactional(readOnly = true)
    public List<OwnerDTO> getOwnersByCity(String city) {
        log.info("Buscando propietarios en la ciudad: {}", city);
        return ownerRepository.findByCityAndIsActiveTrueOrderByLastNameAsc(city)
                .stream()
                .map(owner -> {
                    User user = owner.getUserId() != null ?
                            userRepository.findById(owner.getUserId()).orElse(null) : null;
                    return mapToDTO(owner, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Mapear entidad a DTO
     */
    private OwnerDTO mapToDTO(Owner owner, User user) {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setFirstName(owner.getFirstName());
        dto.setLastName(owner.getLastName());
        dto.setFullName(owner.getFullName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        dto.setAlternativePhone(owner.getAlternativePhone());
        dto.setAddress(owner.getAddress());
        dto.setCity(owner.getCity());
        dto.setPostalCode(owner.getPostalCode());
        dto.setDocumentType(owner.getDocumentType());
        dto.setDocumentNumber(owner.getDocumentNumber());
        dto.setNotes(owner.getNotes());
        dto.setIsActive(owner.getIsActive());
        dto.setTotalPatients(owner.getPatients() != null ? owner.getPatients().size() : 0);
        dto.setUsername(user != null ? user.getUsername() : null);
        dto.setUserId(owner.getUserId() != null ? owner.getUserId().toString() : null);
        dto.setCreatedAt(owner.getCreatedAt());
        dto.setUpdatedAt(owner.getUpdatedAt());
        return dto;
    }
}