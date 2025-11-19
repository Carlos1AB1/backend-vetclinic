package com.vetclinic.service;

import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import com.vetclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(UUID.randomUUID())
                .name("RECEPTIONIST")
                .description("Receptionist role")
                .isSystemRole(true)
                .permissions(new HashSet<>())
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(testRole);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .roles(roles)
                .isActive(true)
                .build();
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsernameWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertTrue(result.isEnabled());

        verify(userRepository).findByUsernameWithRoles(anyString());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsernameWithRoles(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found with username"));
        verify(userRepository).findByUsernameWithRoles(anyString());
    }

    @Test
    void loadUserByUsername_WithRoles_Success() {
        // Arrange
        when(userRepository.findByUsernameWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertFalse(result.getAuthorities().isEmpty());
        verify(userRepository).findByUsernameWithRoles(anyString());
    }

    @Test
    void loadUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmailWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), testUser.getEmail());

        verify(userRepository).findByEmailWithRoles(anyString());
    }

    @Test
    void loadUserByEmail_UserNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmailWithRoles(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByEmail("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("User not found with email"));
        verify(userRepository).findByEmailWithRoles(anyString());
    }

    @Test
    void loadUserByUsername_InactiveUser_ReturnsUserDetails() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByUsernameWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        // El servicio devuelve el usuario incluso si está inactivo
        // La verificación de si está activo se hace en otro lugar (AuthService)
        verify(userRepository).findByUsernameWithRoles(anyString());
    }

    @Test
    void loadUserByUsername_MultipleRoles_Success() {
        // Arrange
        Role adminRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Admin role")
                .isSystemRole(true)
                .permissions(new HashSet<>())
                .build();

        testUser.getRoles().add(adminRole);

        when(userRepository.findByUsernameWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.getAuthorities().size() >= 2);
        verify(userRepository).findByUsernameWithRoles(anyString());
    }

    @Test
    void loadUserByEmail_WithRoles_Success() {
        // Arrange
        when(userRepository.findByEmailWithRoles(anyString()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertFalse(result.getAuthorities().isEmpty());
        verify(userRepository).findByEmailWithRoles(anyString());
    }
}