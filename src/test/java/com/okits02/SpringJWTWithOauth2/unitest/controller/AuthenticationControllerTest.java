package com.okits02.SpringJWTWithOauth2.unitest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.JOSEException;
import com.okits02.SpringJWTWithOauth2.controller.AuthenticationController;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.LogoutRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.service.AuthenticationService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthenticationController.class, properties = "server.servlet.context-path=")
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void login_validRequest_returnsAuthenticationResponse() throws Exception {
        AuthenticationRequest request = buildAuthenticationRequest();
        AuthenticationResponse response = buildAuthenticationResponse();
        when(authenticationService.authentication(any(AuthenticationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").value(response.getToken()))
                .andExpect(jsonPath("$.result.refreshToken").value(response.getRefreshToken()))
                .andExpect(jsonPath("$.result.role").value(response.getRole()));

        verify(authenticationService).authentication(any(AuthenticationRequest.class));
    }

    @Test
    void login_invalidUsername_returnsValidationErrorAndNoServiceCall() throws Exception {
        AuthenticationRequest request = buildAuthenticationRequest();
        request.setUsername("abc");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_USERNAME.getCode()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_invalidPassword_returnsValidationErrorAndNoServiceCall() throws Exception {
        AuthenticationRequest request = buildAuthenticationRequest();
        request.setPassword("weakpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD.getCode()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_wrongCredential_returnsUnauthorized() throws Exception {
        AuthenticationRequest request = buildAuthenticationRequest();
        when(authenticationService.authentication(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHENTICATED.getMessage()));

        verify(authenticationService).authentication(any(AuthenticationRequest.class));
    }

    @Test
    void logout_success_returns200WithMessage() throws Exception {
        LogoutRequest request = buildLogoutRequest();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Successfully logged out"));

        verify(authenticationService).logout(any(LogoutRequest.class));
    }

    @Test
    void logout_refreshTokenNotFound_returnsNotFound() throws Exception {
        LogoutRequest request = buildLogoutRequest();
        doThrow(new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND))
                .when(authenticationService)
                .logout(any(LogoutRequest.class));

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        verify(authenticationService).logout(any(LogoutRequest.class));
    }

    @Test
    void introspect_success_returnsAuthState() throws Exception {
        IntrospectRequest request = buildIntrospectRequest();
        IntrospectResponse response = buildIntrospectResponse(true);
        when(authenticationService.introSpect(any(IntrospectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.authenticated").value(true));

        verify(authenticationService).introSpect(any(IntrospectRequest.class));
    }

    @Test
    void introspect_serviceThrowsJoseException_returnsStandardizedErrorPayload() throws Exception {
        IntrospectRequest request = buildIntrospectRequest();
        when(authenticationService.introSpect(any(IntrospectRequest.class)))
                .thenThrow(new JOSEException("Invalid signature"));

        mockMvc.perform(post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        verify(authenticationService).introSpect(any(IntrospectRequest.class));
    }

    @Test
    void refreshToken_success_returnsNewAccessAndRefreshToken() throws Exception {
        RefreshTokenRequest request = buildRefreshTokenRequest();
        AuthenticationResponse response = buildAuthenticationResponse();
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").value(response.getToken()))
                .andExpect(jsonPath("$.result.refreshToken").value(response.getRefreshToken()));

        verify(authenticationService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void refreshToken_notFound_returnsNotFound() throws Exception {
        RefreshTokenRequest request = buildRefreshTokenRequest();
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        verify(authenticationService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void refreshToken_serviceThrowsParseException_returnsStandardizedErrorPayload() throws Exception {
        RefreshTokenRequest request = buildRefreshTokenRequest();
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new ParseException("Malformed token", 0));

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        verify(authenticationService).refreshToken(any(RefreshTokenRequest.class));
    }

    private AuthenticationRequest buildAuthenticationRequest() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("phamtu13");
        request.setPassword("Phamtu@1304");
        return request;
    }

    private AuthenticationResponse buildAuthenticationResponse() {
        return AuthenticationResponse.builder()
                .token("access-token")
                .authenticated(true)
                .refreshToken("refresh-token")
                .build();
    }

    private LogoutRequest buildLogoutRequest() {
        LogoutRequest request = new LogoutRequest();
        request.setToken("access-token");
        request.setRefreshToken("refresh-token");
        return request;
    }

    private IntrospectRequest buildIntrospectRequest() {
        return IntrospectRequest.builder().token("access-token").build();
    }

    private IntrospectResponse buildIntrospectResponse(boolean authenticated) {
        return IntrospectResponse.builder()
                .authenticated(authenticated)
                .role("ROLE_USER")
                .build();
    }

    private RefreshTokenRequest buildRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken("expired-access-token");
        request.setRefreshToken("refresh-token");
        return request;
    }
}
