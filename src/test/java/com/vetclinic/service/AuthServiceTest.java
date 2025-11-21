package com.vetclinic.service;

import com.vetclinic.dto.auth.*;
import com.vetclinic.entity.PasswordResetToken;
import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BadRequestException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.exception.TokenExpiredException;
import com.vetclinic.exception.UnauthorizedException;
import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import com.vetclinic.patterns.chain.RegistrationValidationChain;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.repository.PasswordResetTokenRepository;
import com.vetclinic.repository.RoleRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailServiceAdapter emailServiceAdapter;

    @Mock
    private RegistrationValidationChain validationChain;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(authService, "tokenExpirationMs", 3600000L);

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
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPhone("9876543210");
    }

    @Test
    void login_Success() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_RECEPTIONIST"),
                        new SimpleGrantedAuthority("USER_READ")
                )
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getType());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertTrue(response.getRoles().contains("RECEPTIONIST"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
        verify(jwtTokenProvider).generateRefreshToken(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_FailedAttempt_IncrementsCounter() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByUsername(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ValidationFails_ThrowsBadRequestException() {
        // Arrange
        when(validationChain.validate(any(RegisterRequest.class)))
                .thenReturn(ValidationResult.failure("Validation failed"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));

        verify(validationChain).validate(any(RegisterRequest.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_NotRefreshToken_ThrowsBadRequestException() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("accessToken");

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.refreshToken(request));

        verify(jwtTokenProvider).validateToken(anyString());
        verify(jwtTokenProvider).isRefreshToken(anyString());
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));

        verify(userRepository).findByEmail(anyString());
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_InvalidToken_ThrowsBadRequestException() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalidToken");
        request.setNewPassword("NewPassword123!");

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));

        verify(tokenRepository).findByToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_TokenAlreadyUsed_ThrowsBadRequestException() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("usedToken");
        request.setNewPassword("NewPassword123!");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("usedToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .usedAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(resetToken));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.resetPassword(request));

        verify(tokenRepository).findByToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_TokenExpired_ThrowsTokenExpiredException() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expiredToken");
        request.setNewPassword("NewPassword123!");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("expiredToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(resetToken));

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> authService.resetPassword(request));

        verify(tokenRepository).findByToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsBadRequestException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("NewPassword123!");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> authService.changePassword(request, "testuser"));

        verify(userRepository).findByUsername(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("NewPassword123!");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> authService.changePassword(request, "nonexistent"));

        verify(userRepository).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}