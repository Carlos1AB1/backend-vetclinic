package com.vetclinic.repository;

import com.vetclinic.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Obtener usuarios del equipo (excluyendo propietarios/clientes)
     * Solo devuelve ADMIN, VETERINARIAN, RECEPTIONIST
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN ('ADMIN', 'VETERINARIAN', 'RECEPTIONIST') AND u.isActive = true")
    Page<User> findTeamMembers(Pageable pageable);

    /**
     * Obtener solo veterinarios activos
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = 'VETERINARIAN' AND u.isActive = true ORDER BY u.lastName, u.firstName")
    List<User> findVeterinarians();
}
