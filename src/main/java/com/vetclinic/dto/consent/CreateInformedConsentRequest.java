package com.vetclinic.dto.consent;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO para crear un consentimiento informado
 * RF016 - Gestión de Consentimientos Informados
 */
@Data
public class CreateInformedConsentRequest {

    @NotNull(message = "El ID del paciente es requerido")
    private Long patientId;

    @NotNull(message = "El ID del propietario es requerido")
    private Long ownerId;

    @NotNull(message = "El ID del veterinario es requerido")
    private java.util.UUID veterinarianId;

    private Long appointmentId;

    @NotBlank(message = "El tipo de procedimiento es requerido")
    @Size(max = 200, message = "El tipo de procedimiento no puede exceder 200 caracteres")
    private String procedureType;

    @NotBlank(message = "La descripción del procedimiento es requerida")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String procedureDescription;

    @Size(max = 2000, message = "Los riesgos no pueden exceder 2000 caracteres")
    private String risks;

    @Size(max = 2000, message = "Los beneficios no pueden exceder 2000 caracteres")
    private String benefits;

    @Size(max = 2000, message = "Las alternativas no pueden exceder 2000 caracteres")
    private String alternatives;
}



