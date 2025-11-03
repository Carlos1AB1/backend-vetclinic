package com.vetclinic.patterns.chain.validators;

import com.vetclinic.dto.auth.RegisterRequest;
import com.vetclinic.patterns.chain.ValidationHandler;
import com.vetclinic.patterns.chain.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Password strength validator
 */
@Component
public class PasswordStrengthValidator extends ValidationHandler<RegisterRequest> {

    private static final Pattern PASSWORD_PATTERN = 
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    @Override
    public ValidationResult validate(RegisterRequest request) {
        String password = request.getPassword();
        
        if (password.length() < 6) {
            return ValidationResult.failure("Password must be at least 6 characters long");
        }
        
        // Optional: Enforce stronger password policy
        // if (!PASSWORD_PATTERN.matcher(password).matches()) {
        //     return ValidationResult.failure(
        //         "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
        //     );
        // }
        
        return checkNext(request);
    }
}
