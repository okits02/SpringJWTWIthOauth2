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
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;

import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class PermissionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void save_success_persistsPermissionInContainerizedMySql() throws Exception {
        String permissionName = nextName("PERM_CREATE_");
        String accessToken = createUserAndAuthenticate(nextUsername("pcs"), "Strong@123");

        PermissionDTO request = new PermissionDTO();
        request.setName(permissionName);
        request.setDescription("Create permission integration test");

        mockMvc.perform(post("/indentity/permissions")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(permissionName))
                .andExpect(jsonPath("$.result.description").value(request.getDescription()));

        Permission savedPermission =
                permissionRepository.findByName(permissionName).orElseThrow();
        assertThat(savedPermission.getDescription()).isEqualTo(request.getDescription());
    }

    @Test
    void update_success_overridesBodyNameWithPathVariableAndPersists() throws Exception {
        String originalName = nextName("PERM_UPDATE_");
        String pathName = nextName("PERM_PATH_");
        String accessToken = createUserAndAuthenticate(nextUsername("pup"), "Strong@123");

        Permission seededPermission = Permission.builder()
                .name(pathName)
                .description("Old description")
                .build();
        permissionRepository.save(seededPermission);

        PermissionDTO request = new PermissionDTO();
        request.setName(originalName);
        request.setDescription("Updated description");

        mockMvc.perform(put("/indentity/permissions/{name}", pathName)
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(pathName))
                .andExpect(jsonPath("$.result.description").value("Updated description"));

        Permission updatedPermission = permissionRepository.findByName(pathName).orElseThrow();
        assertThat(updatedPermission.getDescription()).isEqualTo("Updated description");
        assertThat(permissionRepository.findByName(originalName)).isEmpty();
    }

    @Test
    void findByName_success_returnsPermissionPayload() throws Exception {
        String permissionName = nextName("PERM_FIND_");
        String accessToken = createUserAndAuthenticate(nextUsername("pfb"), "Strong@123");

        permissionRepository.save(Permission.builder()
                .name(permissionName)
                .description("Find by name")
                .build());

        mockMvc.perform(get("/indentity/permissions/{name}", permissionName)
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value(permissionName))
                .andExpect(jsonPath("$.result.description").value("Find by name"));
    }

    @Test
    void findAll_success_containsCreatedPermission() throws Exception {
        String permissionName = nextName("PERM_LIST_");
        String accessToken = createUserAndAuthenticate(nextUsername("pfl"), "Strong@123");

        permissionRepository.save(Permission.builder()
                .name(permissionName)
                .description("List item")
                .build());

        mockMvc.perform(get("/indentity/permissions")
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result[*].name", hasItem(permissionName)));
    }

    @Test
    void delete_success_removesPermissionFromContainerizedMySql() throws Exception {
        String permissionName = nextName("PERM_DEL_");
        String accessToken = createUserAndAuthenticate(nextUsername("pds"), "Strong@123");

        permissionRepository.save(Permission.builder()
                .name(permissionName)
                .description("To be deleted")
                .build());

        mockMvc.perform(delete("/indentity/permissions/{name}", permissionName)
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Permission deleted successfully"));

        assertThat(permissionRepository.findByName(permissionName)).isEmpty();
    }

    @Test
    void deleteAll_success_clearsAllPermissions() throws Exception {
        String permissionName01 = nextName("PERM_ALL_A_");
        String permissionName02 = nextName("PERM_ALL_B_");
        String accessToken = createUserAndAuthenticate(nextUsername("pda"), "Strong@123");

        for (Role role : roleRepository.findAll()) {
            role.setPermissions(new HashSet<>());
            roleRepository.save(role);
        }

        permissionRepository.save(
                Permission.builder().name(permissionName01).description("one").build());
        permissionRepository.save(
                Permission.builder().name(permissionName02).description("two").build());

        mockMvc.perform(delete("/indentity/permissions")
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("All permissions deleted successfully"));

        assertThat(permissionRepository.findByName(permissionName01)).isEmpty();
        assertThat(permissionRepository.findByName(permissionName02)).isEmpty();
    }

    private String createUserAndAuthenticate(String userName, String password) throws Exception {
        UserRequest request = new UserRequest(
                userName,
                password,
                userName + "@gmail.com",
                "0912345678",
                "Integration",
                "Permission",
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

    private String nextName(String prefix) {
        return prefix
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
