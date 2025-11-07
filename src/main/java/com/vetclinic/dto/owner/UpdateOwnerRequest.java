package com.vetclinic.dto.owner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de propietarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOwnerRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String lastName;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{9,20}$", message = "El teléfono debe contener entre 9 y 20 dígitos (puede incluir + al inicio)")
    private String phone;

    @Pattern(regexp = "^[+]?[0-9]{9,20}$", message = "El teléfono alternativo debe contener entre 9 y 20 dígitos (puede incluir + al inicio)")
    private String alternativePhone;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    private String postalCode;

    @Size(max = 20, message = "El tipo de documento no puede exceder 20 caracteres")
    private String documentType;

    @Size(max = 50, message = "El número de documento no puede exceder 50 caracteres")
    private String documentNumber;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    private Boolean isActive;
}
