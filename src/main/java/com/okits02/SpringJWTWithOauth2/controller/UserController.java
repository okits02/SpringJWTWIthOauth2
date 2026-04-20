package com.okits02.SpringJWTWithOauth2.controller;

import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserCreationRequest;
import com.okits02.SpringJWTWithOauth2.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUsers(@RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Resister user successfully!")
                .result(userService.save(request))
                .build();
    }
}
