package com.vetclinic.dto.consent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para representar un consentimiento informado
 * RF016 - Gestión de Consentimientos Informados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformedConsentDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long ownerId;
    private String ownerName;
    private UUID veterinarianId;
    private String veterinarianName;
    private Long appointmentId;
    private String procedureType;
    private String procedureDescription;
    private String risks;
    private String benefits;
    private String alternatives;
    private String ownerSignature;
    private LocalDateTime signedDate;
    private Boolean isSigned;
    private String consentDocumentPath;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



