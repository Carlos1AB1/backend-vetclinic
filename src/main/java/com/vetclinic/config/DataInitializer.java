package com.vetclinic.config;

import com.vetclinic.entity.Permission;
import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import com.vetclinic.repository.PermissionRepository;
import com.vetclinic.repository.RoleRepository;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Data Initializer
 * Creates default roles, permissions, and admin user on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing default data...");

        // Create permissions
        Set<Permission> permissions = createPermissions();

        // Create roles
        createRoles(permissions);

        // Create default admin user
        createDefaultAdmin();

        log.info("Default data initialization completed!");
    }

    private Set<Permission> createPermissions() {
        log.info("Creating permissions...");

        Set<Permission> permissions = new HashSet<>();

        // User permissions
        permissions.add(createPermissionIfNotExists("USER_READ", "users", "read", "Read user information"));
        permissions.add(createPermissionIfNotExists("USER_WRITE", "users", "write", "Create and update users"));
        permissions.add(createPermissionIfNotExists("USER_DELETE", "users", "delete", "Delete users"));

        // Patient permissions
        permissions.add(createPermissionIfNotExists("PATIENT_READ", "patients", "read", "Read patient information"));
        permissions.add(createPermissionIfNotExists("PATIENT_WRITE", "patients", "write", "Create and update patients"));
        permissions.add(createPermissionIfNotExists("PATIENT_DELETE", "patients", "delete", "Delete patients"));

        // Owner permissions
        permissions.add(createPermissionIfNotExists("OWNER_READ", "owners", "read", "Read owner information"));
        permissions.add(createPermissionIfNotExists("OWNER_WRITE", "owners", "write", "Create and update owners"));
        permissions.add(createPermissionIfNotExists("OWNER_DELETE", "owners", "delete", "Delete owners"));

        // Appointment permissions
        permissions.add(createPermissionIfNotExists("APPOINTMENT_READ", "appointments", "read", "Read appointments"));
        permissions.add(createPermissionIfNotExists("APPOINTMENT_WRITE", "appointments", "write", "Create and update appointments"));
        permissions.add(createPermissionIfNotExists("APPOINTMENT_DELETE", "appointments", "delete", "Delete appointments"));

        // Medical record permissions
        permissions.add(createPermissionIfNotExists("MEDICAL_RECORD_READ", "medical_records", "read", "Read medical records"));
        permissions.add(createPermissionIfNotExists("MEDICAL_RECORD_WRITE", "medical_records", "write", "Create and update medical records"));

        // Prescription permissions
        permissions.add(createPermissionIfNotExists("PRESCRIPTION_READ", "prescriptions", "read", "Read prescriptions"));
        permissions.add(createPermissionIfNotExists("PRESCRIPTION_WRITE", "prescriptions", "write", "Create and update prescriptions"));

        // Inventory permissions
        permissions.add(createPermissionIfNotExists("INVENTORY_READ", "inventory", "read", "Read inventory"));
        permissions.add(createPermissionIfNotExists("INVENTORY_WRITE", "inventory", "write", "Manage inventory"));

        // Report permissions
        permissions.add(createPermissionIfNotExists("REPORT_READ", "reports", "read", "View reports"));
        permissions.add(createPermissionIfNotExists("REPORT_GENERATE", "reports", "generate", "Generate reports"));

        log.info("Created {} permissions", permissions.size());
        return permissions;
    }

    private Permission createPermissionIfNotExists(String name, String resource, String action, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = Permission.builder()
                            .name(name)
                            .resource(resource)
                            .action(action)
                            .description(description)
                            .build();
                    return permissionRepository.save(permission);
                });
    }

    private void createRoles(Set<Permission> allPermissions) {
        log.info("Creating roles...");

        // ADMIN role - all permissions
        createRoleIfNotExists(
                "ADMIN",
                "Administrator with full access",
                allPermissions,
                true
        );

        // VETERINARIAN role
        Set<Permission> vetPermissions = new HashSet<>();
        vetPermissions.addAll(findPermissionsByResource("patients"));
        vetPermissions.addAll(findPermissionsByResource("owners"));
        vetPermissions.addAll(findPermissionsByResource("appointments"));
        vetPermissions.addAll(findPermissionsByResource("medical_records"));
        vetPermissions.addAll(findPermissionsByResource("prescriptions"));
        vetPermissions.add(findPermissionByName("INVENTORY_READ"));
        vetPermissions.add(findPermissionByName("REPORT_READ"));

        createRoleIfNotExists(
                "VETERINARIAN",
                "Veterinarian with medical access",
                vetPermissions,
                true
        );

        // RECEPTIONIST role
        Set<Permission> receptionistPermissions = new HashSet<>();
        receptionistPermissions.addAll(findPermissionsByResource("patients"));
        receptionistPermissions.addAll(findPermissionsByResource("owners"));
        receptionistPermissions.addAll(findPermissionsByResource("appointments"));
        receptionistPermissions.add(findPermissionByName("MEDICAL_RECORD_READ"));
        receptionistPermissions.add(findPermissionByName("INVENTORY_READ"));

        createRoleIfNotExists(
                "RECEPTIONIST",
                "Receptionist with limited access",
                receptionistPermissions,
                true
        );

        log.info("Roles created successfully");

        // OWNER role
        Set<Permission> ownerPermissions = new HashSet<>();
        ownerPermissions.add(findPermissionByName("APPOINTMENT_READ"));
        ownerPermissions.add(findPermissionByName("APPOINTMENT_WRITE"));
        ownerPermissions.add(findPermissionByName("PATIENT_READ"));
        ownerPermissions.add(findPermissionByName("PATIENT_WRITE"));

        createRoleIfNotExists(
                "OWNER",
                "Propietario de mascotas con acceso al portal de clientes",
                ownerPermissions,
                true
        );

        log.info("Roles created successfully");
    }

    private void createRoleIfNotExists(String name, String description, Set<Permission> permissions, boolean isSystemRole) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .permissions(permissions)
                    .isSystemRole(isSystemRole)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", name);
        }
    }

    private Set<Permission> findPermissionsByResource(String resource) {
        return new HashSet<>(permissionRepository.findAll().stream()
                .filter(p -> resource.equals(p.getResource()))
                .toList());
    }

    private Permission findPermissionByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    private void createDefaultAdmin() {
        // Check if admin exists
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        
        if (existingAdmin.isPresent()) {
            // Update admin if it exists but is disabled
            User admin = existingAdmin.get();
            if (!admin.getIsActive() || admin.getIsLocked()) {
                admin.setIsActive(true);
                admin.setIsLocked(false);
                admin.setFailedLoginAttempts(0);
                admin.setLockTime(null);
                userRepository.save(admin);
                log.info("Admin user was disabled - Re-enabled successfully");
            } else {
                log.info("Admin user already exists and is active");
            }
            return;
        }

        // Create new admin user
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User admin = User.builder()
                .username("admin")
                .email("admin@vetclinic.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("System")
                .lastName("Administrator")
                .phone("1234567890")
                .roles(Set.of(adminRole))
                .isActive(true)
                .isLocked(false)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created - Username: admin, Password: admin123");
        log.warn("IMPORTANT: Please change the admin password after first login!");
    }
}
