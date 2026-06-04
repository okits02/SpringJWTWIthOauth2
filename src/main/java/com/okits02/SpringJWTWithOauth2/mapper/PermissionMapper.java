package com.okits02.SpringJWTWithOauth2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toPermissionDTO(Permission permission);

    Permission toPermission(PermissionDTO permissionDTO);

    void updatePermission(@MappingTarget Permission permission, PermissionDTO permissionDTO);
}
