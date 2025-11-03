package com.vetclinic.patterns.chain;

import com.vetclinic.dto.auth.RegisterRequest;
import com.vetclinic.patterns.chain.validators.EmailUniquenessValidator;
import com.vetclinic.patterns.chain.validators.PasswordStrengthValidator;
import com.vetclinic.patterns.chain.validators.UsernameUniquenessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Registration Validation Chain
 * Chains all registration validators together
 */
@Component
@RequiredArgsConstructor
public class RegistrationValidationChain {

    private final UsernameUniquenessValidator usernameValidator;
    private final EmailUniquenessValidator emailValidator;
    private final PasswordStrengthValidator passwordValidator;

    public ValidationResult validate(RegisterRequest request) {
        // Build the chain
        usernameValidator.setNext(emailValidator)
                        .setNext(passwordValidator);
        
        // Execute validation
        return usernameValidator.validate(request);
    }
}
