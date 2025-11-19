package com.vetclinic.repository;

import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testRole = Role.builder()
                .name("TEST_USER_ROLE")
                .description("Test Role")
                .isSystemRole(false)
                .permissions(new HashSet<>())
                .build();
        testRole = entityManager.persist(testRole);

        Set<Role> roles = new HashSet<>();
        roles.add(testRole);

        testUser = User.builder()
                .username("testuserrepo")
                .email("testuserrepo@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();
        testUser = entityManager.persist(testUser);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("testuserrepo");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuserrepo");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuserrepo@example.com");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("testuserrepo@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuserrepo@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuserrepo");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return true when username exists")
    void shouldReturnTrueWhenUsernameExists() {
        Boolean exists = userRepository.existsByUsername("testuserrepo");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        Boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        Boolean exists = userRepository.existsByEmail("testuserrepo@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        Boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find user by username with roles")
    void shouldFindUserByUsernameWithRoles() {
        Optional<User> foundUser = userRepository.findByUsernameWithRoles("testuserrepo");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuserrepo");
        assertThat(foundUser.get().getRoles()).isNotEmpty();
    }

    @Test
    @DisplayName("Should find user by email with roles")
    void shouldFindUserByEmailWithRoles() {
        Optional<User> foundUser = userRepository.findByEmailWithRoles("testuserrepo@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuserrepo@example.com");
        assertThat(foundUser.get().getRoles()).isNotEmpty();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        User newUser = User.builder()
                .username("newuserrepo")
                .email("newuserrepo@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuserrepo");
        assertThat(savedUser.getEmail()).isEqualTo("newuserrepo@example.com");
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        testUser.setFirstName("Updated");
        testUser.setLastName("Name");

        User updatedUser = userRepository.save(testUser);

        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        userRepository.delete(testUser);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByUsername("testuserrepo");

        assertThat(foundUser).isEmpty();
    }
}