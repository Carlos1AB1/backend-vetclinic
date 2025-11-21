package com.vetclinic.controller;

import com.vetclinic.dto.ApiResponse;
import com.vetclinic.dto.user.CreateUserRequest;
import com.vetclinic.dto.user.UpdateUserRequest;
import com.vetclinic.dto.user.UserDTO;
import com.vetclinic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Management Controller
 * Admin-only endpoints for user management
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "02. Usuarios", description = "Gestión de usuarios del sistema (Solo Administradores)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar todos los usuarios",
        description = "Obtiene una lista paginada de todos los usuarios del sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado - Solo administradores")
    })
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
        @Parameter(description = "Parámetros de paginación (page, size, sort)") Pageable pageable
    ) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear nuevo usuario",
        description = "Crea una nueva cuenta de usuario en el sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Usuario ya existe")
    })
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
        @Parameter(description = "Datos del nuevo usuario", required = true)
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Obtiene los detalles de un usuario específico por su ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
        @Parameter(description = "ID del usuario", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user)
        );
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener usuario por nombre de usuario",
        description = "Obtiene los detalles de un usuario específico por su username"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(
        @Parameter(description = "Nombre de usuario", required = true, example = "admin")
        @PathVariable String username
    ) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Datos actualizados del usuario", required = true)
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", user)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar usuario",
        description = "Desactiva una cuenta de usuario (eliminación lógica)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable UUID id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Desbloquear usuario",
        description = "Desbloquea una cuenta de usuario bloqueada por intentos fallidos"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario desbloqueado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> unlockUser(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable UUID id
    ) {
        userService.unlockUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User unlocked successfully", null)
        );
    }
}
