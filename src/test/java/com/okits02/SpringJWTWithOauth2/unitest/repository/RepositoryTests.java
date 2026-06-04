package com.okits02.SpringJWTWithOauth2.unitest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTests {
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("identity_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerMysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void findByName_whenRoleExists_returnsRole() {
        Role role = Role.builder().name("MANAGER").build();
        roleRepository.save(role);

        var result = roleRepository.findByName("MANAGER");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MANAGER");
    }

    @Test
    void findByName_whenRoleDoesNotExist_returnsEmpty() {
        var result = roleRepository.findByName("CUSTOMER");

        assertThat(result).isEmpty();
    }

    @Test
    void findByName_whenPermissionExists_returnsPermission() {
        Permission permission = Permission.builder().name("REGISTER_USER").build();
        permissionRepository.save(permission);
        var result = permissionRepository.findByName("REGISTER_USER");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("REGISTER_USER");
    }

    @Test
    void findByName_whenPermissionDoesNotExist_returnsEmpty() {
        var result = permissionRepository.findByName("CUSTOMER");
        assertThat(result).isEmpty();
    }

    @Test
    void findByName_whenUserExists_returnsUser() {
        Users user = Users.builder()
                .userName("test")
                .password("test")
                .email("test1304.c@gmail.com")
                .phone("098623162")
                .firstName("test")
                .lastName("test")
                .build();
        userRepository.save(user);

        var result = userRepository.findByUserName("test");
        assertThat(result).isPresent();
        assertThat(result.get().getUserName()).isEqualTo("test");
    }

    @Test
    void findByName_whenUserDoesNotExist_returnsEmpty() {
        var result = userRepository.findByUserName("test1");
        assertThat(result).isEmpty();
    }
}
