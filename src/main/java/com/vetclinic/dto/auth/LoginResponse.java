package com.vetclinic.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private List<String> permissions;
}
