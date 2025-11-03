package com.vetclinic.patterns.chain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    
    private boolean valid;
    private String message;
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    public static ValidationResult failure(String message) {
        return ValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }

    public static ValidationResult failure(String message, List<String> errors) {
        return ValidationResult.builder()
                .valid(false)
                .message(message)
                .errors(errors)
                .build();
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}
