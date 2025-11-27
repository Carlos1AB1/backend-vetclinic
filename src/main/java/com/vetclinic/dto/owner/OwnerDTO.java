package com.vetclinic.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de propietarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String alternativePhone;
    private String address;
    private String city;
    private String postalCode;
    private String documentType;
    private String documentNumber;
    private String notes;
    private Boolean isActive;
    private Integer totalPatients;
    private String username;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}