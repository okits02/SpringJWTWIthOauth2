package com.okits02.SpringJWTWithOauth2.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RolePermissionRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;

import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class RoleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void createRole_success_persistsRoleInContainerizedMySql() throws Exception {
        String roleName = nextRoleName();
        String accessToken = createUserAndAuthenticate(nextUsername("rcs"), "Strong@123");

        RoleDTO request =
                RoleDTO.builder().name(roleName).permissions(new HashSet<>()).build();

        mockMvc.perform(post("/indentity/roles")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(roleName));

        assertThat(roleRepository.findByName(roleName)).isPresent();
    }

    @Test
    void updateRole_success_updatesPermissions() throws Exception {
        String roleName = nextRoleName();
        String permissionName = nextPermissionName();
        String accessToken = createUserAndAuthenticate(nextUsername("rus"), "Strong@123");

        roleRepository.save(
                Role.builder().name(roleName).permissions(new HashSet<>()).build());
        permissionRepository.save(Permission.builder()
                .name(permissionName)
                .description("Role update permission")
                .build());

        RoleDTO request = RoleDTO.builder()
                .name(roleName)
                .permissions(Set.of(permissionDto(permissionName, "Role update permission")))
                .build();

        mockMvc.perform(put("/indentity/roles/{roleName}", roleName)
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(roleName))
                .andExpect(jsonPath("$.result.permissions[*].name", hasItem(permissionName)));

        Role updatedRole = roleRepository.findByName(roleName).orElseThrow();
        assertThat(updatedRole.getPermissions()).extracting(Permission::getName).contains(permissionName);
    }

    @Test
    void deactivateRole_success_deletesRoleFromContainerizedMySql() throws Exception {
        String roleName = nextRoleName();
        String accessToken = createUserAndAuthenticate(nextUsername("rds"), "Strong@123");

        roleRepository.save(
                Role.builder().name(roleName).permissions(new HashSet<>()).build());

        mockMvc.perform(delete("/indentity/roles/{roleName}", roleName)
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));

        assertThat(roleRepository.findByName(roleName)).isEmpty();
    }

    @Test
    void getRoleById_success_returnsRolePayload() throws Exception {
        String roleName = nextRoleName();
        String accessToken = createUserAndAuthenticate(nextUsername("rgb"), "Strong@123");

        roleRepository.save(
                Role.builder().name(roleName).permissions(new HashSet<>()).build());

        mockMvc.perform(get("/indentity/roles/{roleName}", roleName)
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(roleName));
    }

    @Test
    void getAllRoles_success_containsCreatedRoles() throws Exception {
        String roleName01 = nextRoleName();
        String roleName02 = nextRoleName();
        String accessToken = createUserAndAuthenticate(nextUsername("rgl"), "Strong@123");

        roleRepository.save(
                Role.builder().name(roleName01).permissions(new HashSet<>()).build());
        roleRepository.save(
                Role.builder().name(roleName02).permissions(new HashSet<>()).build());

        mockMvc.perform(get("/indentity/roles")
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result[*].name", hasItem(roleName01)))
                .andExpect(jsonPath("$.result[*].name", hasItem(roleName02)));
    }

    @Test
    void addPermissionToRole_success_appendsPermission() throws Exception {
        String roleName = nextRoleName();
        String permissionName = nextPermissionName();
        String accessToken = createUserAndAuthenticate(nextUsername("rap"), "Strong@123");

        roleRepository.save(
                Role.builder().name(roleName).permissions(new HashSet<>()).build());
        permissionRepository.save(Permission.builder()
                .name(permissionName)
                .description("Attach permission")
                .build());

        RolePermissionRequest request = new RolePermissionRequest();
        request.setPermissionName(permissionName);

        mockMvc.perform(post("/indentity/roles/{roleName}/permissions", roleName)
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(roleName))
                .andExpect(jsonPath("$.result.permissions[*].name", hasItem(permissionName)));

        Role updatedRole = roleRepository.findByName(roleName).orElseThrow();
        assertThat(updatedRole.getPermissions()).extracting(Permission::getName).contains(permissionName);
    }

    private PermissionDTO permissionDto(String name, String description) {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName(name);
        permissionDTO.setDescription(description);
        return permissionDTO;
    }

    private String createUserAndAuthenticate(String userName, String password) throws Exception {
        UserRequest request = new UserRequest(
                userName,
                password,
                userName + "@gmail.com",
                "0912345678",
                "Integration",
                "Role",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userName, password);

        String loginResponse = mockMvc.perform(post("/indentity/auth/login")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).at("/result/token").asText();
    }

    private String nextUsername(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 7);
        return (prefix + suffix).substring(0, 10);
    }

    private String nextRoleName() {
        return "ROLE_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String nextPermissionName() {
        return "PERM_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
