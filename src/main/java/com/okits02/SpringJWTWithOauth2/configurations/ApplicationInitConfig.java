package com.okits02.SpringJWTWithOauth2.configurations;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.okits02.SpringJWTWithOauth2.constant.PredefinedPermission;
import com.okits02.SpringJWTWithOauth2.constant.PredefinedRole;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    PermissionRepository permissionRepository;

    @NonFinal
    static final String ADMIN_USERNAME_DEFINE = "admin";

    @NonFinal
    static final String ADMIN_PASS_DEFINE = "Admin@123";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            Role adminRole = ensureRole(roleRepository, PredefinedRole.ROLE_SUPPER_ADMIN);
            ensureRole(roleRepository, PredefinedRole.ROLE_USER);
            if (userRepository.findByUserName(ADMIN_USERNAME_DEFINE).isEmpty()) {
                var roles = new HashSet<Role>();
                roles.add(adminRole);
                Users user = Users.builder()
                        .userName(ADMIN_USERNAME_DEFINE)
                        .password(passwordEncoder.encode(ADMIN_PASS_DEFINE))
                        .email("admin123@gmail.com")
                        .roles(roles)
                        .build();
                userRepository.save(user);
            }
        };
    }

    private Role ensureRole(RoleRepository roleRepository, String roleName) {
        return roleRepository.findById(roleName).orElseGet(() -> {
            Set<Permission> permissionsSuperAdmin = new HashSet<>();
            if (PredefinedRole.ROLE_SUPPER_ADMIN.equals(roleName)) {
                permissionsSuperAdmin.add(ensurePermission(PredefinedPermission.PERMISSION_ADMIN));
            }
            return roleRepository.save(Role.builder()
                    .name(roleName)
                    .permissions(permissionsSuperAdmin)
                    .build());
        });
    }

    private Permission ensurePermission(String permissionName) {
        return permissionRepository
                .findByName(permissionName)
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .name(PredefinedPermission.PERMISSION_ADMIN)
                        .build()));
    }
}
