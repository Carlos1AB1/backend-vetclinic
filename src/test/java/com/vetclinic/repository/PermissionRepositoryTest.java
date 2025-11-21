package com.vetclinic.repository;

import com.vetclinic.entity.Permission;
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
@DisplayName("PermissionRepository Tests")
class PermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission userReadPermission;
    private Permission userWritePermission;
    private Permission userDeletePermission;

    @BeforeEach
    void setUp() {
        permissionRepository.deleteAll();

        userReadPermission = Permission.builder()
                .name("TEST_PERM_USER_READ")
                .resource("users")
                .action("read")
                .description("Read user information")
                .build();
        userReadPermission = entityManager.persist(userReadPermission);

        userWritePermission = Permission.builder()
                .name("TEST_PERM_USER_WRITE")
                .resource("users")
                .action("write")
                .description("Create and update users")
                .build();
        userWritePermission = entityManager.persist(userWritePermission);

        userDeletePermission = Permission.builder()
                .name("TEST_PERM_USER_DELETE")
                .resource("users")
                .action("delete")
                .description("Delete users")
                .build();
        userDeletePermission = entityManager.persist(userDeletePermission);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find permission by name")
    void shouldFindPermissionByName() {
        Optional<Permission> foundPermission = permissionRepository.findByName("TEST_PERM_USER_READ");

        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getName()).isEqualTo("TEST_PERM_USER_READ");
        assertThat(foundPermission.get().getResource()).isEqualTo("users");
        assertThat(foundPermission.get().getAction()).isEqualTo("read");
    }

    @Test
    @DisplayName("Should return empty when permission name not found")
    void shouldReturnEmptyWhenPermissionNameNotFound() {
        Optional<Permission> foundPermission = permissionRepository.findByName("NONEXISTENT_PERMISSION");

        assertThat(foundPermission).isEmpty();
    }

    @Test
    @DisplayName("Should find permissions by name in")
    void shouldFindPermissionsByNameIn() {
        Set<String> permissionNames = new HashSet<>();
        permissionNames.add("TEST_PERM_USER_READ");
        permissionNames.add("TEST_PERM_USER_WRITE");

        Set<Permission> foundPermissions = permissionRepository.findByNameIn(permissionNames);

        assertThat(foundPermissions).isNotEmpty();
        assertThat(foundPermissions).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty set when no permissions match names")
    void shouldReturnEmptySetWhenNoPermissionsMatchNames() {
        Set<String> permissionNames = new HashSet<>();
        permissionNames.add("NONEXISTENT1");
        permissionNames.add("NONEXISTENT2");

        Set<Permission> foundPermissions = permissionRepository.findByNameIn(permissionNames);

        assertThat(foundPermissions).isEmpty();
    }

    @Test
    @DisplayName("Should return true when permission name exists")
    void shouldReturnTrueWhenPermissionNameExists() {
        Boolean exists = permissionRepository.existsByName("TEST_PERM_USER_READ");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when permission name does not exist")
    void shouldReturnFalseWhenPermissionNameDoesNotExist() {
        Boolean exists = permissionRepository.existsByName("NONEXISTENT_PERMISSION");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save permission successfully")
    void shouldSavePermissionSuccessfully() {
        Permission newPermission = Permission.builder()
                .name("TEST_PERM_PATIENT_READ")
                .resource("patients")
                .action("read")
                .description("Read patient information")
                .build();

        Permission savedPermission = permissionRepository.save(newPermission);

        assertThat(savedPermission).isNotNull();
        assertThat(savedPermission.getId()).isNotNull();
        assertThat(savedPermission.getName()).isEqualTo("TEST_PERM_PATIENT_READ");
        assertThat(savedPermission.getResource()).isEqualTo("patients");
    }

    @Test
    @DisplayName("Should update permission successfully")
    void shouldUpdatePermissionSuccessfully() {
        userReadPermission.setDescription("Updated description for user read");

        Permission updatedPermission = permissionRepository.save(userReadPermission);

        assertThat(updatedPermission.getDescription()).isEqualTo("Updated description for user read");
    }

    @Test
    @DisplayName("Should delete permission successfully")
    void shouldDeletePermissionSuccessfully() {
        permissionRepository.delete(userDeletePermission);
        entityManager.flush();

        Optional<Permission> foundPermission = permissionRepository.findByName("TEST_PERM_USER_DELETE");

        assertThat(foundPermission).isEmpty();
    }

    @Test
    @DisplayName("Should find partial list of permissions by name in")
    void shouldFindPartialListOfPermissionsByNameIn() {
        Set<String> permissionNames = new HashSet<>();
        permissionNames.add("TEST_PERM_USER_READ");
        permissionNames.add("TEST_PERM_USER_WRITE");
        permissionNames.add("NONEXISTENT");

        Set<Permission> foundPermissions = permissionRepository.findByNameIn(permissionNames);

        assertThat(foundPermissions).hasSizeGreaterThanOrEqualTo(2);
    }
}