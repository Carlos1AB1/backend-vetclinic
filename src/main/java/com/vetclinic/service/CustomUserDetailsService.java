package com.vetclinic.service;

import com.vetclinic.entity.User;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation
 * Loads user details from database for Spring Security
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username
                ));

        log.debug("User found: {}, roles: {}", username, user.getRoles().size());
        
        return user;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
    }
}
