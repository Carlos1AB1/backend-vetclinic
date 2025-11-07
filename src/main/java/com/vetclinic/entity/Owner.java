package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Owner (Propietario)
 * Representa a los dueños de las mascotas
 */
@Entity
@Table(name = "owners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 20)
    private String alternativePhone;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 20)
    private String documentType; // DNI, Pasaporte, etc.

    @Column(length = 50)
    private String documentNumber;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Método helper para obtener nombre completo
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Método helper para agregar paciente
    public void addPatient(Patient patient) {
        patients.add(patient);
        patient.setOwner(this);
    }

    // Método helper para remover paciente
    public void removePatient(Patient patient) {
        patients.remove(patient);
        patient.setOwner(null);
    }
}
