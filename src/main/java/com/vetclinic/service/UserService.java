package com.vetclinic.service;

import com.vetclinic.dto.user.CreateUserRequest;
import com.vetclinic.dto.user.UpdateUserRequest;
import com.vetclinic.dto.user.UserDTO;
import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BadRequestException;
import com.vetclinic.exception.ResourceNotFoundException;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Service
 * Manages user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        // Get or create roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles = roleRepository.findByNameInWithPermissions(request.getRoles());
            if (roles.isEmpty()) {
                throw new BadRequestException("Invalid roles provided");
            }
        } else {
            // Default role if not provided
            Role defaultRole = roleRepository.findByName("RECEPTIONIST")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "RECEPTIONIST"));
            roles.add(defaultRole);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        user = userRepository.save(user);
        log.info("User created: {}", user.getUsername());

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = roleRepository.findByNameInWithPermissions(request.getRoles());
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getUsername());

        return mapToDTO(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Soft delete by deactivating
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User deactivated: {}", user.getUsername());
    }

    @Transactional
    public void unlockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        
        log.info("User unlocked: {}", user.getUsername());
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .isLocked(user.getIsLocked())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
