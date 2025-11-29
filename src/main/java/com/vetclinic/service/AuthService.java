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
import com.vetclinic.patterns.adapter.SmsServiceAdapter;
import com.vetclinic.patterns.chain.RegistrationValidationChain;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.patterns.factory.NotificationFactory;
import com.vetclinic.repository.PasswordResetTokenRepository;
import com.vetclinic.repository.RoleRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles user authentication, registration, and password management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailServiceAdapter emailServiceAdapter;
    private final SmsServiceAdapter smsServiceAdapter;
    private final RegistrationValidationChain validationChain;
    private final NotificationFactory notificationFactory;
    private final EmailTemplateService emailTemplateService;
    private final SmsTemplateService smsTemplateService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.password-reset-token-expiration}")
    private long tokenExpirationMs;

    /**
     * Authenticate user and generate JWT tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = (User) authentication.getPrincipal();

            // Update last login and reset failed attempts
            user.setLastLogin(LocalDateTime.now());
            user.resetFailedLoginAttempts();
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            // Extract roles and permissions
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            log.info("User logged in successfully: {}", user.getUsername());

            return LoginResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            // Handle failed login attempt
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(user -> {
                        user.incrementFailedLoginAttempts();
                        userRepository.save(user);
                    });

            log.error("Login failed for user: {}", request.getUsername());
            throw e;
        }
    }

    /**
     * Register new user
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Validate registration
        ValidationResult validationResult = validationChain.validate(request);
        if (!validationResult.isValid()) {
            throw new BadRequestException(validationResult.getMessage());
        }

        // Get or create roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles = roleRepository.findByNameInWithPermissions(request.getRoles());
        } else {
            // Default role
            Role defaultRole = roleRepository.findByName("RECEPTIONIST")
                    .orElseGet(() -> createDefaultRole("RECEPTIONIST"));
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
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Send welcome email
        try {
            sendWelcomeEmail(user);
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }

        // Auto-login after registration
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Token is not a refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }

    /**
     * Request password reset
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Delete existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Create new reset token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenExpirationMs / 1000);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(resetToken);

        // Send reset email
        sendPasswordResetEmail(user, token);

        log.info("Password reset requested for user: {}", user.getEmail());
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());

        // Send confirmation email
        try {
            sendPasswordChangedEmail(user);
        } catch (Exception e) {
            log.error("Failed to send password changed email", e);
        }
    }

    /**
     * Change password for authenticated user
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);

        // Send confirmation email
        try {
            sendPasswordChangedEmail(user);
        } catch (Exception e) {
            log.error("Failed to send password changed email", e);
        }
    }

    // Helper methods

    private Role createDefaultRole(String roleName) {
        Role role = Role.builder()
                .name(roleName)
                .description("Default " + roleName + " role")
                .isSystemRole(true)
                .build();
        return roleRepository.save(role);
    }

    private void sendWelcomeEmail(User user) {
        String subject = "¡Bienvenido a VetClinic Pro!";
        String loginUrl = frontendUrl + "/login";
        String htmlBody = emailTemplateService.getWelcomeEmailTemplate(
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                loginUrl
        );

        // Enviar email HTML
        try {
            emailServiceAdapter.sendHtmlEmail(user.getEmail(), subject, htmlBody);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    private void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Restablecer Contraseña - VetClinic Pro";
        String htmlBody = emailTemplateService.getPasswordResetEmailTemplate(
                user.getFullName(),
                resetLink
        );

        // Enviar email HTML
        try {
            emailServiceAdapter.sendHtmlEmail(user.getEmail(), subject, htmlBody);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
        
        // Enviar SMS
        if (user.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getPasswordResetSms(user.getFullName(), resetLink);
                smsServiceAdapter.sendSms(user.getPhone(), smsMessage);
                log.info("✅ SMS de restablecimiento de contraseña enviado a: {}", user.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de restablecimiento de contraseña a: {}", user.getPhone(), e);
            }
        }
    }

    private void sendPasswordChangedEmail(User user) {
        String subject = "Contraseña Actualizada - VetClinic Pro";
        String htmlBody = emailTemplateService.getPasswordChangedEmailTemplate(
                user.getFullName()
        );

        // Enviar email HTML
        try {
            emailServiceAdapter.sendHtmlEmail(user.getEmail(), subject, htmlBody);
            log.info("Password changed email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {}", user.getEmail(), e);
        }
        
        // Enviar SMS
        if (user.getPhone() != null && smsServiceAdapter.isAvailable()) {
            try {
                String smsMessage = smsTemplateService.getPasswordChangedSms(user.getFullName());
                smsServiceAdapter.sendSms(user.getPhone(), smsMessage);
                log.info("✅ SMS de contraseña actualizada enviado a: {}", user.getPhone());
            } catch (Exception e) {
                log.error("Error al enviar SMS de contraseña actualizada a: {}", user.getPhone(), e);
            }
        }
    }
}
