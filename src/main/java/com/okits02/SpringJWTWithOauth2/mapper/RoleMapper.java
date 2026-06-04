package com.okits02.SpringJWTWithOauth2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDTO toRoleDTO(Role role);

    Role fromRoleDTO(RoleDTO roleDTO);

    @Mapping(target = "permissions", ignore = true)
    void updateRole(@MappingTarget Role role, RoleDTO roleDTO);
}
