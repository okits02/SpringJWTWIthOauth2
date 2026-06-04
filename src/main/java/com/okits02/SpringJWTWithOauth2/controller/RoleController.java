package com.okits02.SpringJWTWithOauth2.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RolePermissionRequest;
import com.okits02.SpringJWTWithOauth2.service.RoleService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PreAuthorize("hasAuthority('ALL')")
    @PostMapping
    ApiResponse<RoleDTO> createRole(@RequestBody @Valid RoleDTO request) {
        return ApiResponse.<RoleDTO>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @PutMapping("/{roleName}")
    ApiResponse<RoleDTO> updateRole(@PathVariable String roleName, @RequestBody @Valid RoleDTO request) {
        return ApiResponse.<RoleDTO>builder()
                .result(roleService.updateRole(roleName, request))
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @DeleteMapping("/{roleName}")
    ApiResponse<Void> deactivateRole(@PathVariable String roleName) {
        roleService.deactivateRole(roleName);
        return ApiResponse.<Void>builder().message("Role deleted successfully").build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @GetMapping("/{roleName}")
    ApiResponse<RoleDTO> getRoleById(@PathVariable String roleName) {
        return ApiResponse.<RoleDTO>builder()
                .result(roleService.getRoleById(roleName))
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @GetMapping
    ApiResponse<List<RoleDTO>> getAllRoles() {
        return ApiResponse.<List<RoleDTO>>builder()
                .result(roleService.getAllRoles())
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @PostMapping("/{roleName}/permissions")
    ApiResponse<RoleDTO> addPermissionToRole(
            @PathVariable String roleName, @RequestBody RolePermissionRequest request) {
        return ApiResponse.<RoleDTO>builder()
                .result(roleService.addPermissionToRole(roleName, request.getPermissionName()))
                .build();
    }
}
