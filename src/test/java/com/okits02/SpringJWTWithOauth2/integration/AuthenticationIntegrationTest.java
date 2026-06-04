package com.okits02.SpringJWTWithOauth2.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.LogoutRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_validRequest_returnsAccessAndRefreshToken() throws Exception {
        String userName = nextUsername("alg");
        String password = "Strong@123";
        createUser(userName, password);

        AuthenticationRequest request = new AuthenticationRequest(userName, password);

        mockMvc.perform(post("/indentity/auth/login")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").isNotEmpty())
                .andExpect(jsonPath("$.result.refreshToken").isNotEmpty());
    }

    @Test
    void login_wrongCredential_returnsUnauthorized() throws Exception {
        String userName = nextUsername("alw");
        String password = "Strong@123";
        createUser(userName, password);

        AuthenticationRequest request = new AuthenticationRequest(userName, "Wrongpass1!");

        mockMvc.perform(post("/indentity/auth/login")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHENTICATED.getMessage()));
    }

    @Test
    void introspect_validToken_returnsAuthenticatedTrue() throws Exception {
        String userName = nextUsername("ait");
        String password = "Strong@123";
        createUser(userName, password);
        AuthTokenPair tokens = loginAndGetTokenPair(userName, password);

        IntrospectRequest request =
                IntrospectRequest.builder().token(tokens.accessToken()).build();

        mockMvc.perform(post("/indentity/auth/introspect")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    @Test
    void refreshToken_currentImplementation_returnsTokenNotFound() throws Exception {
        String userName = nextUsername("arf");
        String password = "Strong@123";
        createUser(userName, password);
        AuthTokenPair originalTokens = loginAndGetTokenPair(userName, password);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken(originalTokens.accessToken());
        request.setRefreshToken(originalTokens.refreshToken());

        mockMvc.perform(post("/indentity/auth/refresh-token")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.TOKEN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TOKEN_NOT_FOUND.getMessage()));
    }

    @Test
    void logout_currentImplementation_returnsUncategorizedError() throws Exception {
        String userName = nextUsername("alo");
        String password = "Strong@123";
        createUser(userName, password);
        AuthTokenPair tokens = loginAndGetTokenPair(userName, password);

        LogoutRequest logoutRequest = new LogoutRequest(tokens.accessToken(), tokens.refreshToken());

        mockMvc.perform(post("/indentity/auth/logout")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));
    }

    private void createUser(String userName, String password) throws Exception {
        UserRequest request = new UserRequest(
                userName,
                password,
                userName + "@gmail.com",
                "0912345678",
                "Integration",
                "Auth",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private AuthTokenPair loginAndGetTokenPair(String userName, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(userName, password);

        String loginResponse = mockMvc.perform(post("/indentity/auth/login")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode result = objectMapper.readTree(loginResponse).path("result");
        return new AuthTokenPair(
                result.path("token").asText(), result.path("refreshToken").asText());
    }

    private String nextUsername(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 7);
        return (prefix + suffix).substring(0, 10);
    }

    private record AuthTokenPair(String accessToken, String refreshToken) {}
}
