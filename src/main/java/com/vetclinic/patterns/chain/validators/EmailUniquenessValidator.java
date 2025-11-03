package com.vetclinic.patterns.chain.validators;

import com.vetclinic.dto.auth.RegisterRequest;
import com.vetclinic.patterns.chain.ValidationHandler;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Email uniqueness validator
 */
@Component
@RequiredArgsConstructor
public class EmailUniquenessValidator extends ValidationHandler<RegisterRequest> {

    private final UserRepository userRepository;

    @Override
    public ValidationResult validate(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ValidationResult.failure("Email is already registered");
        }
        return checkNext(request);
    }
}
