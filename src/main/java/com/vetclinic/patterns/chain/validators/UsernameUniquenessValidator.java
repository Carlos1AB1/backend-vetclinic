package com.vetclinic.patterns.chain.validators;

import com.vetclinic.dto.auth.RegisterRequest;
import com.vetclinic.patterns.chain.ValidationHandler;
import com.vetclinic.patterns.chain.ValidationResult;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Username uniqueness validator
 */
@Component
@RequiredArgsConstructor
public class UsernameUniquenessValidator extends ValidationHandler<RegisterRequest> {

    private final UserRepository userRepository;

    @Override
    public ValidationResult validate(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ValidationResult.failure("Username is already taken");
        }
        return checkNext(request);
    }
}
