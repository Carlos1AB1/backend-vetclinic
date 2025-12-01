package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad para representar consentimientos informados
 * RF016 - Gestión de Consentimientos Informados
 */
@Entity
@Table(name = "informed_consents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InformedConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private User veterinarian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "procedure_type", nullable = false, length = 200)
    private String procedureType; // SURGERY, ANESTHESIA, VACCINATION, etc.

    @Column(name = "procedure_description", nullable = false, columnDefinition = "TEXT")
    private String procedureDescription;

    @Column(name = "risks", columnDefinition = "TEXT")
    private String risks;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "alternatives", columnDefinition = "TEXT")
    private String alternatives;

    @Column(name = "owner_signature", length = 500)
    private String ownerSignature; // Base64 o referencia a archivo

    @Column(name = "signed_date")
    private LocalDateTime signedDate;

    @Column(name = "is_signed", nullable = false)
    private Boolean isSigned = false;

    @Column(name = "consent_document_path", length = 500)
    private String consentDocumentPath; // Ruta al PDF generado

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}



