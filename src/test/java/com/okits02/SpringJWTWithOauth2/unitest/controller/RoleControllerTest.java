package com.okits02.SpringJWTWithOauth2.unitest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.controller.RoleController;
import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RolePermissionRequest;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.service.RoleService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = RoleController.class, properties = "server.servlet.context-path=")
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @Test
    void createRole_success_returnsRolePayload() throws Exception {
        RoleDTO request = buildRole("MANAGER");
        when(roleService.createRole(any(RoleDTO.class))).thenReturn(request);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value("MANAGER"))
                .andExpect(jsonPath("$.result.permissions.length()").value(1));

        verify(roleService).createRole(any(RoleDTO.class));
    }

    @Test
    void createRole_roleExists_returnsConflict() throws Exception {
        RoleDTO request = buildRole("MANAGER");
        when(roleService.createRole(any(RoleDTO.class))).thenThrow(new AppException(ErrorCode.ROLE_EXISTS));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROLE_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROLE_EXISTS.getMessage()));

        verify(roleService).createRole(any(RoleDTO.class));
    }

    @Test
    void updateRole_success_returnsUpdatedRole() throws Exception {
        String roleName = "MANAGER";
        RoleDTO request = buildRole(roleName);
        when(roleService.updateRole(eq(roleName), any(RoleDTO.class))).thenReturn(request);

        mockMvc.perform(put("/roles/{roleName}", roleName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value(roleName))
                .andExpect(jsonPath("$.result.permissions[0].name").value("USER_READ"));

        verify(roleService).updateRole(eq(roleName), any(RoleDTO.class));
    }

    @Test
    void updateRole_roleNotFound_returnsNotFound() throws Exception {
        String roleName = "MISSING_ROLE";
        RoleDTO request = buildRole(roleName);
        when(roleService.updateRole(eq(roleName), any(RoleDTO.class)))
                .thenThrow(new AppException(ErrorCode.ROLE_NOT_FOUND));

        mockMvc.perform(put("/roles/{roleName}", roleName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROLE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROLE_NOT_FOUND.getMessage()));

        verify(roleService).updateRole(eq(roleName), any(RoleDTO.class));
    }

    @Test
    void deactivateRole_success_returnsDeleteMessage() throws Exception {
        mockMvc.perform(delete("/roles/{roleName}", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));

        verify(roleService).deactivateRole("MANAGER");
    }

    @Test
    void deactivateRole_roleNotFound_returnsNotFound() throws Exception {
        doThrow(new AppException(ErrorCode.ROLE_NOT_FOUND)).when(roleService).deactivateRole(eq("MISSING_ROLE"));

        mockMvc.perform(delete("/roles/{roleName}", "MISSING_ROLE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROLE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROLE_NOT_FOUND.getMessage()));

        verify(roleService).deactivateRole("MISSING_ROLE");
    }

    @Test
    void getRoleById_success_returnsRole() throws Exception {
        RoleDTO role = buildRole("MANAGER");
        when(roleService.getRoleById(eq("MANAGER"))).thenReturn(role);

        mockMvc.perform(get("/roles/{roleName}", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("MANAGER"))
                .andExpect(jsonPath("$.result.permissions.length()").value(1));

        verify(roleService).getRoleById("MANAGER");
    }

    @Test
    void getRoleById_roleNotFound_returnsNotFound() throws Exception {
        when(roleService.getRoleById(eq("MISSING_ROLE"))).thenThrow(new AppException(ErrorCode.ROLE_NOT_FOUND));

        mockMvc.perform(get("/roles/{roleName}", "MISSING_ROLE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ROLE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROLE_NOT_FOUND.getMessage()));

        verify(roleService).getRoleById("MISSING_ROLE");
    }

    @Test
    void getAllRoles_success_returnsRoleList() throws Exception {
        RoleDTO manager = buildRole("MANAGER");
        RoleDTO auditor = buildRole("AUDITOR");
        when(roleService.getAllRoles()).thenReturn(java.util.List.of(manager, auditor));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].name").value("MANAGER"))
                .andExpect(jsonPath("$.result[1].name").value("AUDITOR"));

        verify(roleService).getAllRoles();
    }

    @Test
    void addPermissionToRole_success_returnsUpdatedRole() throws Exception {
        String roleName = "MANAGER";
        String permissionName = "USER_WRITE";
        RolePermissionRequest request = buildRolePermissionRequest(permissionName);
        RoleDTO response = buildRole(roleName);
        when(roleService.addPermissionToRole(eq(roleName), eq(permissionName))).thenReturn(response);

        mockMvc.perform(post("/roles/{roleName}/permissions", roleName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value(roleName));

        verify(roleService).addPermissionToRole(roleName, permissionName);
    }

    @Test
    void addPermissionToRole_permissionNotFound_returnsNotFound() throws Exception {
        String roleName = "MANAGER";
        String permissionName = "MISSING_PERMISSION";
        RolePermissionRequest request = buildRolePermissionRequest(permissionName);
        when(roleService.addPermissionToRole(eq(roleName), eq(permissionName)))
                .thenThrow(new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        mockMvc.perform(post("/roles/{roleName}/permissions", roleName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PERMISSION_NOT_FOUND.getMessage()));

        verify(roleService).addPermissionToRole(roleName, permissionName);
    }

    @Test
    void getAllRoles_unexpectedError_returnsStandardizedPayload() throws Exception {
        when(roleService.getAllRoles()).thenThrow(new RuntimeException("cache unavailable"));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        verify(roleService).getAllRoles();
    }

    private RoleDTO buildRole(String roleName) {
        PermissionDTO permission = new PermissionDTO();
        permission.setName("USER_READ");
        permission.setDescription("Read user data");

        return RoleDTO.builder().name(roleName).permissions(Set.of(permission)).build();
    }

    private RolePermissionRequest buildRolePermissionRequest(String permissionName) {
        RolePermissionRequest request = new RolePermissionRequest();
        request.setPermissionName(permissionName);
        return request;
    }
}
