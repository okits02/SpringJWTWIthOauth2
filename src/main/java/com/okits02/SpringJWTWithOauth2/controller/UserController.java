package com.okits02.SpringJWTWithOauth2.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.okits02.SpringJWTWithOauth2.dto.ChangePassDTO;
import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.PageResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;
import com.okits02.SpringJWTWithOauth2.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
@Tag(name = "User Controller", description = "API for user-related operation, ")
public class UserController {
    UserService userService;

    @Operation(summary = "Register user")
    @PreAuthorize("hasAnyAuthority('ALL', 'USER_CREATE', 'PROFILE_POST')")
    @PostMapping
    ApiResponse<UserResponse> createUsers(@RequestBody @Valid UserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Resister user successfully!")
                .result(userService.save(request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_UPDATE')")
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_UPDATE', 'PROFILE_UPDATE')")
    @PutMapping("/me")
    ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserInfoDTO request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyInfo(request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_UPDATE', 'PROFILE_UPDATE')")
    @PutMapping("/change-password")
    ApiResponse<ChangePassDTO> changePassword(@RequestBody @Valid ChangePassDTO request) {
        return ApiResponse.<ChangePassDTO>builder()
                .result(userService.changePassword(request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_READE', 'PROFILE_READE')")
    @GetMapping("/me")
    ApiResponse<UserInfoDTO> getMyInfo() {
        return ApiResponse.<UserInfoDTO>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_READE')")
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getById(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getById(userId))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ALL', 'USER_CREATE')")
    @GetMapping
    ApiResponse<PageResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> usersPage = userService.getAll(page, size);
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .currentPage(usersPage.getNumber())
                .totalPage(usersPage.getTotalPages())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .data(usersPage.getContent())
                .build();

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(pageResponse)
                .build();
    }
}
