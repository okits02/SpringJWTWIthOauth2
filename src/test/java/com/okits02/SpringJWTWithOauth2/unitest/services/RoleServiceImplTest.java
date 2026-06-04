package com.okits02.SpringJWTWithOauth2.unitest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.RoleMapper;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.service.impl.RoleServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRole_whenRoleExists_throwsRoleExists() {
        RoleDTO request = buildRoleDTO("MANAGER", Set.of());
        when(roleRepository.existsByName("MANAGER")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ROLE_EXISTS);
    }

    @Test
    void createRole_whenValidRequest_returnsCreatedRole() {
        RoleDTO request = buildRoleDTO("MANAGER", Set.of());
        Role role = buildRole("MANAGER", Set.of());

        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleMapper.fromRoleDTO(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleDTO(role)).thenReturn(request);

        RoleDTO result = roleService.createRole(request);

        assertThat(result).isEqualTo(request);
        verify(roleRepository).save(role);
    }

    @Test
    void updateRole_whenRoleNotFound_throwsRoleNotFound() {
        RoleDTO request = buildRoleDTO("MANAGER", Set.of());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole("MANAGER", request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void updateRole_whenPermissionListContainsMissingPermission_throwsPermissionNotFound() {
        PermissionDTO read = buildPermissionDTO("USER_READ");
        PermissionDTO write = buildPermissionDTO("USER_WRITE");
        RoleDTO request = buildRoleDTO("MANAGER", Set.of(read, write));
        Role role = buildRole("MANAGER", Set.of());

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(buildPermission("USER_READ")));

        assertThatThrownBy(() -> roleService.updateRole("MANAGER", request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void updateRole_whenPermissionsValid_updatesRolePermissions() {
        PermissionDTO read = buildPermissionDTO("USER_READ");
        PermissionDTO write = buildPermissionDTO("USER_WRITE");
        RoleDTO request = buildRoleDTO("MANAGER", Set.of(read, write));
        Role role = buildRole("MANAGER", Set.of());
        Permission p1 = buildPermission("USER_READ");
        Permission p2 = buildPermission("USER_WRITE");
        RoleDTO expected = buildRoleDTO("MANAGER", Set.of(read, write));

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleDTO(role)).thenReturn(expected);

        RoleDTO result = roleService.updateRole("MANAGER", request);

        assertThat(result).isEqualTo(expected);
        assertThat(role.getPermissions()).containsExactlyInAnyOrder(p1, p2);
        verify(roleMapper).updateRole(role, request);
    }

    @Test
    void deactivateRole_whenRoleNotFound_throwsRoleNotFound() {
        when(roleRepository.findByName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deactivateRole("MISSING"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void deactivateRole_whenRoleExists_deletesRole() {
        Role role = buildRole("MANAGER", Set.of());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));

        roleService.deactivateRole("MANAGER");

        verify(roleRepository).delete(role);
    }

    @Test
    void getRoleById_whenRoleExists_returnsMappedRole() {
        Role role = buildRole("MANAGER", Set.of());
        RoleDTO dto = buildRoleDTO("MANAGER", Set.of());

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(roleMapper.toRoleDTO(role)).thenReturn(dto);

        RoleDTO result = roleService.getRoleById("MANAGER");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getAllRoles_returnsMappedRoles() {
        Role manager = buildRole("MANAGER", Set.of());
        Role user = buildRole("USER", Set.of());
        RoleDTO managerDto = buildRoleDTO("MANAGER", Set.of());
        RoleDTO userDto = buildRoleDTO("USER", Set.of());

        when(roleRepository.findAll()).thenReturn(List.of(manager, user));
        when(roleMapper.toRoleDTO(manager)).thenReturn(managerDto);
        when(roleMapper.toRoleDTO(user)).thenReturn(userDto);

        List<RoleDTO> result = roleService.getAllRoles();

        assertThat(result).containsExactly(managerDto, userDto);
    }

    @Test
    void addPermissionToRole_whenRoleNotFound_throwsRoleNotFound() {
        when(roleRepository.findByName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.addPermissionToRole("MISSING", "USER_READ"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void addPermissionToRole_whenPermissionNotFound_throwsPermissionNotFound() {
        Role role = buildRole("MANAGER", Set.of());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findByName("MISSING_PERMISSION")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.addPermissionToRole("MANAGER", "MISSING_PERMISSION"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void addPermissionToRole_whenValid_addsPermissionAndReturnsRole() {
        Role role = buildRole("MANAGER", Set.of());
        Permission permission = buildPermission("USER_READ");
        RoleDTO expected = buildRoleDTO("MANAGER", Set.of(buildPermissionDTO("USER_READ")));

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permission));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleDTO(role)).thenReturn(expected);

        RoleDTO result = roleService.addPermissionToRole("MANAGER", "USER_READ");

        assertThat(result).isEqualTo(expected);
        assertThat(role.getPermissions()).contains(permission);
    }

    @Test
    void removePermissionFromRole_whenValid_removesPermissionAndReturnsRole() {
        Permission permission = buildPermission("USER_READ");
        Role role = buildRole("MANAGER", Set.of(permission));
        RoleDTO expected = buildRoleDTO("MANAGER", Set.of());

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permission));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleDTO(role)).thenReturn(expected);

        RoleDTO result = roleService.removePermissionFromRole("MANAGER", "USER_READ");

        assertThat(result).isEqualTo(expected);
        assertThat(role.getPermissions()).doesNotContain(permission);
    }

    private RoleDTO buildRoleDTO(String name, Set<PermissionDTO> permissions) {
        return RoleDTO.builder()
                .name(name)
                .permissions(new java.util.HashSet<>(permissions))
                .build();
    }

    private PermissionDTO buildPermissionDTO(String name) {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName(name);
        permissionDTO.setDescription(name + " desc");
        return permissionDTO;
    }

    private Role buildRole(String name, Set<Permission> permissions) {
        return Role.builder()
                .name(name)
                .permissions(new java.util.HashSet<>(permissions))
                .build();
    }

    private Permission buildPermission(String name) {
        return Permission.builder().name(name).description(name + " desc").build();
    }
}
