package com.okits02.SpringJWTWithOauth2.unitest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.controller.PermissionController;
import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.service.PermissionService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PermissionController.class, properties = "server.servlet.context-path=")
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    void save_success_returnsCreatedPermission() throws Exception {
        PermissionDTO request = buildPermission("USER_READ", "Read user data");
        when(permissionService.save(any(PermissionDTO.class))).thenReturn(request);

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.name").value("USER_READ"))
                .andExpect(jsonPath("$.result.description").value("Read user data"));

        verify(permissionService).save(any(PermissionDTO.class));
    }

    @Test
    void save_permissionExists_returnsConflict() throws Exception {
        PermissionDTO request = buildPermission("USER_READ", "Read user data");
        when(permissionService.save(any(PermissionDTO.class))).thenThrow(new AppException(ErrorCode.PERMISSION_EXISTS));

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PERMISSION_EXISTS.getMessage()));

        verify(permissionService).save(any(PermissionDTO.class));
    }

    @Test
    void update_success_overridesBodyNameByPathVariable() throws Exception {
        PermissionDTO request = buildPermission("SHOULD_BE_OVERRIDDEN", "Updated desc");
        String pathName = "USER_WRITE";
        when(permissionService.update(any(PermissionDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/permissions/{name}", pathName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value(pathName))
                .andExpect(jsonPath("$.result.description").value("Updated desc"));

        ArgumentCaptor<PermissionDTO> captor = ArgumentCaptor.forClass(PermissionDTO.class);
        verify(permissionService).update(captor.capture());
        assertEquals(pathName, captor.getValue().getName());
        assertEquals("Updated desc", captor.getValue().getDescription());
    }

    @Test
    void update_permissionNotFound_returnsNotFound() throws Exception {
        PermissionDTO request = buildPermission("USER_READ", "Updated desc");
        when(permissionService.update(any(PermissionDTO.class)))
                .thenThrow(new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        mockMvc.perform(put("/permissions/{name}", "USER_READ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PERMISSION_NOT_FOUND.getMessage()));

        verify(permissionService).update(any(PermissionDTO.class));
    }

    @Test
    void delete_success_returnsMessageAndForwardsPathVariable() throws Exception {
        String name = "USER_DELETE";

        mockMvc.perform(delete("/permissions/{name}", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permission deleted successfully"));

        ArgumentCaptor<PermissionDTO> captor = ArgumentCaptor.forClass(PermissionDTO.class);
        verify(permissionService).delete(captor.capture());
        assertEquals(name, captor.getValue().getName());
    }

    @Test
    void delete_permissionNotFound_returnsNotFound() throws Exception {
        doThrow(new AppException(ErrorCode.PERMISSION_NOT_FOUND))
                .when(permissionService)
                .delete(any(PermissionDTO.class));

        mockMvc.perform(delete("/permissions/{name}", "MISSING_PERMISSION"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PERMISSION_NOT_FOUND.getMessage()));

        verify(permissionService).delete(any(PermissionDTO.class));
    }

    @Test
    void deleteAll_success_returnsMessage() throws Exception {
        mockMvc.perform(delete("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All permissions deleted successfully"));

        verify(permissionService).deleteAll();
    }

    @Test
    void findAll_success_returnsPermissionList() throws Exception {
        PermissionDTO p1 = buildPermission("USER_READ", "Read user data");
        PermissionDTO p2 = buildPermission("USER_WRITE", "Write user data");
        when(permissionService.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].name").value("USER_READ"))
                .andExpect(jsonPath("$.result[1].name").value("USER_WRITE"));

        verify(permissionService).findAll();
    }

    @Test
    void findAll_unexpectedError_returnsStandardizedPayload() throws Exception {
        when(permissionService.findAll()).thenThrow(new RuntimeException("db temporary unavailable"));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        verify(permissionService).findAll();
    }

    @Test
    void findByName_success_returnsPermission() throws Exception {
        PermissionDTO permission = buildPermission("USER_READ", "Read user data");
        when(permissionService.findByName(eq("USER_READ"))).thenReturn(permission);

        mockMvc.perform(get("/permissions/{name}", "USER_READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("USER_READ"))
                .andExpect(jsonPath("$.result.description").value("Read user data"));

        verify(permissionService).findByName("USER_READ");
    }

    @Test
    void findByName_permissionNotFound_returnsNotFound() throws Exception {
        when(permissionService.findByName(eq("MISSING"))).thenThrow(new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        mockMvc.perform(get("/permissions/{name}", "MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PERMISSION_NOT_FOUND.getMessage()));

        verify(permissionService).findByName("MISSING");
    }

    private PermissionDTO buildPermission(String name, String description) {
        PermissionDTO permission = new PermissionDTO();
        permission.setName(name);
        permission.setDescription(description);
        return permission;
    }
}
