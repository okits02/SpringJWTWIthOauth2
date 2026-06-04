package com.okits02.SpringJWTWithOauth2.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.PermissionMapper;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionDTO save(PermissionDTO request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTS);
        }
        return permissionMapper.toPermissionDTO(permissionRepository.save(permissionMapper.toPermission(request)));
    }

    @Override
    public void delete(PermissionDTO request) {
        Permission permission = permissionRepository
                .findByName(request.getName())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        permissionRepository.delete(permission);
    }

    @Override
    public void deleteAll() {
        permissionRepository.deleteAll();
    }

    @Override
    public PermissionDTO update(PermissionDTO request) {
        Permission permission = permissionRepository
                .findByName(request.getName())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        permissionMapper.updatePermission(permission, request);
        return permissionMapper.toPermissionDTO(permissionRepository.save(permission));
    }

    @Override
    public List<PermissionDTO> findAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionDTO).toList();
    }

    @Override
    public PermissionDTO findByName(String name) {
        Permission permission = permissionRepository
                .findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        return permissionMapper.toPermissionDTO(permission);
    }
}
