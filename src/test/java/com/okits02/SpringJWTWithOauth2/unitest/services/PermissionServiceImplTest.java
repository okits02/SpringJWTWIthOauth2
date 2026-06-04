package com.okits02.SpringJWTWithOauth2.unitest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;
import com.okits02.SpringJWTWithOauth2.entity.Permission;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.PermissionMapper;
import com.okits02.SpringJWTWithOauth2.repository.PermissionRepository;
import com.okits02.SpringJWTWithOauth2.service.impl.PermissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void save_whenPermissionAlreadyExists_throwsPermissionExists() {
        PermissionDTO request = buildPermissionDTO("USER_READ", "Read user data");
        when(permissionRepository.existsByName("USER_READ")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.save(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_EXISTS);

        verify(permissionRepository).existsByName("USER_READ");
        verify(permissionMapper, never()).toPermission(any(PermissionDTO.class));
    }

    @Test
    void save_whenValidRequest_returnsSavedPermission() {
        PermissionDTO request = buildPermissionDTO("USER_READ", "Read user data");
        Permission permission = buildPermission("USER_READ", "Read user data");

        when(permissionRepository.existsByName("USER_READ")).thenReturn(false);
        when(permissionMapper.toPermission(request)).thenReturn(permission);
        when(permissionRepository.save(permission)).thenReturn(permission);
        when(permissionMapper.toPermissionDTO(permission)).thenReturn(request);

        PermissionDTO result = permissionService.save(request);

        assertThat(result).isEqualTo(request);
        verify(permissionRepository).save(permission);
    }

    @Test
    void delete_whenPermissionNotFound_throwsPermissionNotFound() {
        PermissionDTO request = buildPermissionDTO("MISSING", "Missing permission");
        when(permissionRepository.findByName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.delete(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);

        verify(permissionRepository, never()).delete(any(Permission.class));
    }

    @Test
    void delete_whenPermissionExists_deletesPermission() {
        PermissionDTO request = buildPermissionDTO("USER_DELETE", "Delete users");
        Permission permission = buildPermission("USER_DELETE", "Delete users");
        when(permissionRepository.findByName("USER_DELETE")).thenReturn(Optional.of(permission));

        permissionService.delete(request);

        verify(permissionRepository).delete(permission);
    }

    @Test
    void update_whenPermissionNotFound_throwsPermissionNotFound() {
        PermissionDTO request = buildPermissionDTO("MISSING", "desc");
        when(permissionRepository.findByName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.update(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void update_whenPermissionExists_updatesAndReturnsDto() {
        PermissionDTO request = buildPermissionDTO("USER_WRITE", "Write user data");
        Permission permission = buildPermission("USER_WRITE", "Old desc");

        when(permissionRepository.findByName("USER_WRITE")).thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenReturn(permission);
        when(permissionMapper.toPermissionDTO(permission)).thenReturn(request);

        PermissionDTO result = permissionService.update(request);

        assertThat(result).isEqualTo(request);
        verify(permissionMapper).updatePermission(permission, request);
        verify(permissionRepository).save(permission);
    }

    @Test
    void findAll_returnsMappedPermissionList() {
        Permission p1 = buildPermission("USER_READ", "Read");
        Permission p2 = buildPermission("USER_WRITE", "Write");
        PermissionDTO d1 = buildPermissionDTO("USER_READ", "Read");
        PermissionDTO d2 = buildPermissionDTO("USER_WRITE", "Write");

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(permissionMapper.toPermissionDTO(p1)).thenReturn(d1);
        when(permissionMapper.toPermissionDTO(p2)).thenReturn(d2);

        List<PermissionDTO> result = permissionService.findAll();

        assertThat(result).containsExactly(d1, d2);
    }

    @Test
    void findByName_whenNotFound_throwsPermissionNotFound() {
        when(permissionRepository.findByName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.findByName("MISSING"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void findByName_whenFound_returnsMappedDto() {
        Permission permission = buildPermission("USER_READ", "Read");
        PermissionDTO dto = buildPermissionDTO("USER_READ", "Read");

        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permission));
        when(permissionMapper.toPermissionDTO(permission)).thenReturn(dto);

        PermissionDTO result = permissionService.findByName("USER_READ");

        assertThat(result).isEqualTo(dto);
    }

    private PermissionDTO buildPermissionDTO(String name, String description) {
        PermissionDTO dto = new PermissionDTO();
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }

    private Permission buildPermission(String name, String description) {
        return Permission.builder().name(name).description(description).build();
    }
}
