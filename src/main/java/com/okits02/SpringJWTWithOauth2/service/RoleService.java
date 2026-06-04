package com.okits02.SpringJWTWithOauth2.service;

import java.util.List;

import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;

public interface RoleService {
    RoleDTO createRole(RoleDTO request);

    RoleDTO updateRole(String roleName, RoleDTO request);

    void deactivateRole(String roleName);

    RoleDTO getRoleById(String roleName);

    List<RoleDTO> getAllRoles();

    RoleDTO addPermissionToRole(String roleName, String permissionName);

    RoleDTO removePermissionFromRole(String roleName, String permissionName);
}
