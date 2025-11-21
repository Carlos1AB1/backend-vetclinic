package com.vetclinic.repository;

import com.vetclinic.entity.Permission;
import com.vetclinic.entity.Role;
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
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Role adminRole;
    private Role veterinarianRole;
    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        permission1 = Permission.builder()
                .name("TEST_ROLE_USER_READ")
                .resource("users")
                .action("read")
                .description("Read users")
                .build();
        permission1 = entityManager.persist(permission1);

        permission2 = Permission.builder()
                .name("TEST_ROLE_USER_WRITE")
                .resource("users")
                .action("write")
                .description("Write users")
                .build();
        permission2 = entityManager.persist(permission2);

        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(permission1);
        adminPermissions.add(permission2);

        adminRole = Role.builder()
                .name("TEST_ROLE_ADMIN")
                .description("Administrator role")
                .isSystemRole(true)
                .permissions(adminPermissions)
                .build();
        adminRole = entityManager.persist(adminRole);

        Set<Permission> vetPermissions = new HashSet<>();
        vetPermissions.add(permission1);

        veterinarianRole = Role.builder()
                .name("TEST_ROLE_VETERINARIAN")
                .description("Veterinarian role")
                .isSystemRole(true)
                .permissions(vetPermissions)
                .build();
        veterinarianRole = entityManager.persist(veterinarianRole);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find role by name")
    void shouldFindRoleByName() {
        Optional<Role> foundRole = roleRepository.findByName("TEST_ROLE_ADMIN");

        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("TEST_ROLE_ADMIN");
        assertThat(foundRole.get().getDescription()).isEqualTo("Administrator role");
        assertThat(foundRole.get().getIsSystemRole()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when role name not found")
    void shouldReturnEmptyWhenRoleNameNotFound() {
        Optional<Role> foundRole = roleRepository.findByName("NONEXISTENT");

        assertThat(foundRole).isEmpty();
    }

    @Test
    @DisplayName("Should return true when role name exists")
    void shouldReturnTrueWhenRoleNameExists() {
        Boolean exists = roleRepository.existsByName("TEST_ROLE_ADMIN");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when role name does not exist")
    void shouldReturnFalseWhenRoleNameDoesNotExist() {
        Boolean exists = roleRepository.existsByName("NONEXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find roles by name in with permissions")
    void shouldFindRolesByNameInWithPermissions() {
        Set<String> roleNames = new HashSet<>();
        roleNames.add("TEST_ROLE_ADMIN");
        roleNames.add("TEST_ROLE_VETERINARIAN");

        Set<Role> foundRoles = roleRepository.findByNameInWithPermissions(roleNames);

        assertThat(foundRoles).isNotEmpty();
        assertThat(foundRoles).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty set when no roles match names")
    void shouldReturnEmptySetWhenNoRolesMatchNames() {
        Set<String> roleNames = new HashSet<>();
        roleNames.add("NONEXISTENT1");
        roleNames.add("NONEXISTENT2");

        Set<Role> foundRoles = roleRepository.findByNameInWithPermissions(roleNames);

        assertThat(foundRoles).isEmpty();
    }

    @Test
    @DisplayName("Should save role successfully")
    void shouldSaveRoleSuccessfully() {
        Role newRole = Role.builder()
                .name("TEST_ROLE_RECEPTIONIST")
                .description("Receptionist role")
                .isSystemRole(true)
                .permissions(new HashSet<>())
                .build();

        Role savedRole = roleRepository.save(newRole);

        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("TEST_ROLE_RECEPTIONIST");
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() {
        adminRole.setDescription("Updated admin description");

        Role updatedRole = roleRepository.save(adminRole);

        assertThat(updatedRole.getDescription()).isEqualTo("Updated admin description");
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() {
        roleRepository.delete(veterinarianRole);
        entityManager.flush();

        Optional<Role> foundRole = roleRepository.findByName("TEST_ROLE_VETERINARIAN");

        assertThat(foundRole).isEmpty();
    }
}