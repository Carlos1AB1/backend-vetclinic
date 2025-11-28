package com.vetclinic.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt de contraseñas
 * Uso: Ejecutar el método main para generar hashes
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String adminPassword = "admin123";
        String defaultPassword = "password123";
        
        String adminHash = encoder.encode(adminPassword);
        String defaultHash = encoder.encode(defaultPassword);
        
        System.out.println("=== HASHES BCrypt (12 rounds) ===");
        System.out.println("Contraseña: " + adminPassword);
        System.out.println("Hash: " + adminHash);
        System.out.println();
        System.out.println("Contraseña: " + defaultPassword);
        System.out.println("Hash: " + defaultHash);
        System.out.println();
        System.out.println("=== SQL UPDATE ===");
        System.out.println("UPDATE users SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + defaultHash + "' WHERE username != 'admin';");
    }
}

