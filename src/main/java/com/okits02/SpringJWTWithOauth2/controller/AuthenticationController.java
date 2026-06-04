package com.okits02.SpringJWTWithOauth2.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.LogoutRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;
import com.okits02.SpringJWTWithOauth2.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/oauth2/google")
    ApiResponse<AuthenticationResponse> oauth2Authentication(@RequestParam("code") String code) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.oauth2Authentication(code))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authentication(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Successfully logged out")
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introSpect(request))
                .build();
    }

    @PostMapping("/refresh-token")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }
}
