package com.vetclinic.controller;

import com.vetclinic.dto.auth.*;
import com.vetclinic.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private ChangePasswordRequest changePasswordRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("admin")
                .password("password")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        refreshTokenRequest = new RefreshTokenRequest("refresh-token");

        forgotPasswordRequest = new ForgotPasswordRequest("user@test.com");

        resetPasswordRequest = ResetPasswordRequest.builder()
                .token("reset-token")
                .newPassword("newPassword123")
                .build();

        changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        loginResponse = LoginResponse.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .type("Bearer")
                .id("user-id")
                .username("admin")
                .email("admin@test.com")
                .fullName("Admin User")
                .roles(Collections.singletonList("ADMIN"))
                .build();
    }

    @Test
    void testLogin_Success() {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        authController.login(loginRequest);

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testRegister_Success() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(loginResponse);

        authController.register(registerRequest);

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRefreshToken_Success() {
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(loginResponse);

        authController.refreshToken(refreshTokenRequest);

        verify(authService, times(1)).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testForgotPassword_Success() {
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        authController.forgotPassword(forgotPasswordRequest);

        verify(authService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void testResetPassword_Success() {
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        authController.resetPassword(resetPasswordRequest);

        verify(authService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void testChangePassword_Success() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        doNothing().when(authService).changePassword(any(ChangePasswordRequest.class), anyString());

        authController.changePassword(changePasswordRequest, userDetails);

        verify(authService, times(1)).changePassword(any(ChangePasswordRequest.class), eq("testuser"));
    }

    @Test
    void testGetCurrentUser_Success() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        authController.getCurrentUser(userDetails);

    }

    @Test
    void testLogout_Success() {
        authController.logout();

    }
}