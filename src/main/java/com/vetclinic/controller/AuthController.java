package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.auth.*;
import com.vetclinic.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles authentication endpoints: login, register, password management
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "01. Autenticación", description = "Endpoints para autenticación, registro y gestión de contraseñas")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y devuelve tokens JWT (access y refresh token)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Parameter(description = "Credenciales de inicio de sesión", required = true)
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    @PostMapping("/register")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario en el sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Usuario o email ya registrado")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> register(
        @Parameter(description = "Datos de registro del usuario", required = true)
        @Valid @RequestBody RegisterRequest request
    ) {
        LoginResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refrescar token de acceso",
        description = "Obtiene un nuevo access token usando el refresh token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
        @Parameter(description = "Refresh token", required = true)
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response)
        );
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Solicitar restablecimiento de contraseña",
        description = "Envía un email con token para restablecer la contraseña"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email enviado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
        @Parameter(description = "Email del usuario", required = true)
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset email sent successfully", null)
        );
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Restablecer contraseña",
        description = "Restablece la contraseña usando el token recibido por email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
        @Parameter(description = "Token y nueva contraseña", required = true)
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully", null)
        );
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Cambiar contraseña",
        description = "Cambia la contraseña del usuario autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "Contraseña actual y nueva contraseña", required = true)
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.changePassword(request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null)
        );
    }

    @GetMapping("/me")
    @Operation(
        summary = "Obtener usuario actual",
        description = "Obtiene los detalles del usuario autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ApiResponse<UserDetails>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("User details retrieved successfully", userDetails)
        );
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Cerrar sesión",
        description = "Cierra la sesión del usuario actual (el token debe eliminarse en el frontend)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout exitoso")
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless, actual logout is handled on frontend by removing token
        return ResponseEntity.ok(
                ApiResponse.success("Logout successful", null)
        );
    }
}
