package com.okits02.SpringJWTWithOauth2.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.RoleMapper;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @Override
    public RoleDTO createRole(RoleDTO request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTS);
        }
        return roleMapper.toRoleDTO(roleRepository.save(roleMapper.fromRoleDTO(request)));
    }

    @Override
    public RoleDTO updateRole(String roleName, RoleDTO request) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleMapper.updateRole(role, request);

        if (!CollectionUtils.isEmpty(request.getPermissions())) {
            Set<String> permissionNames = request.getPermissions().stream()
                    .map(permissionDTO -> permissionDTO.getName())
                    .collect(java.util.stream.Collectors.toSet());

            List<Permission> permissions = permissionRepository.findAll().stream()
                    .filter(permission -> permissionNames.contains(permission.getName()))
                    .toList();

            if (permissions.size() != permissionNames.size()) {
                throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
            }

            role.setPermissions(new HashSet<>(permissions));
        }

        return roleMapper.toRoleDTO(roleRepository.save(role));
    }

    @Override
    public void deactivateRole(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roleRepository.delete(role);
    }

    @Override
    public RoleDTO getRoleById(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return roleMapper.toRoleDTO(role);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleDTO).toList();
    }

    @Override
    public RoleDTO addPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Permission permission = permissionRepository
                .findByName(permissionName)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        role.getPermissions().add(permission);
        return roleMapper.toRoleDTO(roleRepository.save(role));
    }

    @Override
    public RoleDTO removePermissionFromRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Permission permission = permissionRepository
                .findByName(permissionName)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        role.getPermissions().remove(permission);
        return roleMapper.toRoleDTO(roleRepository.save(role));
    }
}
