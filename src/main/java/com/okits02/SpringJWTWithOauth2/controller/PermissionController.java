package com.okits02.SpringJWTWithOauth2.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import com.okits02.SpringJWTWithOauth2.service.PermissionService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PreAuthorize("hasAuthority('ALL')")
    @PostMapping
    ApiResponse<PermissionDTO> save(@RequestBody @Valid PermissionDTO request) {
        return ApiResponse.<PermissionDTO>builder()
                .result(permissionService.save(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @PutMapping("/{name}")
    ApiResponse<PermissionDTO> update(@PathVariable String name, @RequestBody @Valid PermissionDTO request) {
        request.setName(name);
        return ApiResponse.<PermissionDTO>builder()
                .result(permissionService.update(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @DeleteMapping("/{name}")
    ApiResponse<Void> delete(@PathVariable String name) {
        PermissionDTO request = new PermissionDTO();
        request.setName(name);
        permissionService.delete(request);
        return ApiResponse.<Void>builder()
                .message("Permission deleted successfully")
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @DeleteMapping
    ApiResponse<Void> deleteAll() {
        permissionService.deleteAll();
        return ApiResponse.<Void>builder()
                .message("All permissions deleted successfully")
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @GetMapping
    ApiResponse<List<PermissionDTO>> findAll() {
        return ApiResponse.<List<PermissionDTO>>builder()
                .result(permissionService.findAll())
                .build();
    }

    @PreAuthorize("hasAuthority('ALL')")
    @GetMapping("/{name}")
    ApiResponse<PermissionDTO> findByName(@PathVariable String name) {
        return ApiResponse.<PermissionDTO>builder()
                .result(permissionService.findByName(name))
                .build();
    }
}
