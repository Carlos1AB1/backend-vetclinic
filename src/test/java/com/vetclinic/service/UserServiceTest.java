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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(UUID.randomUUID())
                .name("RECEPTIONIST")
                .description("Receptionist role")
                .isSystemRole(true)
                .permissions(new HashSet<>())
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .roles(Set.of(testRole))
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateUserRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("newuser@example.com");
        createRequest.setPassword("password123");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");
        createRequest.setPhone("9876543210");
        createRequest.setRoles(Set.of("RECEPTIONIST"));
        createRequest.setIsActive(true);

        updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
        updateRequest.setPhone("1111111111");
        updateRequest.setIsActive(true);
        updateRequest.setRoles(Set.of("ADMIN"));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByNameInWithPermissions(any())).thenReturn(Set.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.createUser(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findByUsername(anyString());
        verify(userRepository).findByEmail(anyString());
        verify(roleRepository).findByNameInWithPermissions(any());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.createUser(createRequest));

        verify(userRepository).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.createUser(createRequest));

        verify(userRepository).findByUsername(anyString());
        verify(userRepository).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_InvalidRoles_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByNameInWithPermissions(any())).thenReturn(Collections.emptySet());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.createUser(createRequest));

        verify(userRepository).findByUsername(anyString());
        verify(userRepository).findByEmail(anyString());
        verify(roleRepository).findByNameInWithPermissions(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_NoRolesProvided_UsesDefaultRole() {
        // Arrange
        createRequest.setRoles(null);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("RECEPTIONIST")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.createUser(createRequest);

        // Assert
        assertNotNull(result);
        verify(roleRepository).findByName("RECEPTIONIST");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(userRepository).findByUsername(anyString());
    }

    @Test
    void getUserByUsername_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername("nonexistent"));

        verify(userRepository).findByUsername(anyString());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByNameInWithPermissions(any())).thenReturn(Set.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(userId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(roleRepository).findByNameInWithPermissions(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, updateRequest));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_PartialUpdate_Success() {
        // Arrange
        UUID userId = testUser.getId();
        UpdateUserRequest partialRequest = new UpdateUserRequest();
        partialRequest.setFirstName("UpdatedFirstName");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(userId, partialRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unlockUser_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.unlockUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void unlockUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.unlockUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}