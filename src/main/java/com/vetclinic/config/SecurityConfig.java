package com.vetclinic.config;

import com.vetclinic.security.JwtAuthenticationEntryPoint;
import com.vetclinic.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security Configuration
 * Configures Spring Security with JWT authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Endpoints públicos para acciones desde recordatorios (RF018)
                        .requestMatchers(HttpMethod.GET, "/appointments/{id}/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appointments/{id}/cancel-reminder").permitAll()
                        .requestMatchers(HttpMethod.POST, "/appointments/{id}/reschedule").permitAll()

                        // User endpoints - Specific endpoints first, then admin-only
                        .requestMatchers(HttpMethod.GET, "/users/username/**").hasAnyRole("ADMIN", "VETERINARIAN", "RECEPTIONIST", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN", "VETERINARIAN", "RECEPTIONIST", "OWNER")
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        .requestMatchers("/roles/**").hasRole("ADMIN")
                        // DELETE endpoints - Admin and Veterinarian only
                        .requestMatchers(HttpMethod.DELETE, "/patients/**").hasAnyRole("ADMIN", "VETERINARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/owners/**").hasAnyRole("ADMIN", "VETERINARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasAnyRole("ADMIN", "VETERINARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/medical-records/**").hasAnyRole("ADMIN", "VETERINARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/inventory/**").hasAnyRole("ADMIN", "VETERINARIAN")

                        // Authenticated endpoints
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}