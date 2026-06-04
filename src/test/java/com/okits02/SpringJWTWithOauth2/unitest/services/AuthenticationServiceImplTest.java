package com.okits02.SpringJWTWithOauth2.unitest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.okits02.SpringJWTWithOauth2.constant.PredefinedRole;
import com.okits02.SpringJWTWithOauth2.dto.GoogleUserDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.Oauth2GoogleResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.Oauth2GoogleRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;
import com.okits02.SpringJWTWithOauth2.entity.InvalidatedToken;
import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.InvalidatedTokenRepository;
import com.okits02.SpringJWTWithOauth2.repository.Oauth2.GoogleClient;
import com.okits02.SpringJWTWithOauth2.repository.Oauth2.GoogleUserInfo;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;
import com.okits02.SpringJWTWithOauth2.service.RefreshTokenService;
import com.okits02.SpringJWTWithOauth2.service.impl.AuthenticationServiceImpl;

import feign.FeignException;
import feign.Request;
import feign.Response;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String SIGNER_KEY = "8XhJGP0154kAnQuNfNTLozGf9ZegnFfISt88v4uq3uSIX6N4gyj5AtRhy8XxLK0Q";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GoogleClient googleClient;

    @Mock
    private GoogleUserInfo googleUserInfo;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", SIGNER_KEY);
        ReflectionTestUtils.setField(authenticationService, "JWT_ACCESS_TOKEN_EXPIRATION", 3600000L);
        ReflectionTestUtils.setField(authenticationService, "REFRESH_TOKEN_EXPIRATION_IN_MS", 604800000L);
        ReflectionTestUtils.setField(authenticationService, "CLIENT_ID", "google-client-id");
        ReflectionTestUtils.setField(authenticationService, "CLIENT_SECRET", "google-client-secret");
        ReflectionTestUtils.setField(authenticationService, "REDIRECT_URI", "http://localhost:3000/authenticate");
    }

    @Test
    void oauth2Authentication_whenTokenExchangeFails_throwsOauth2TokenExchangeFailed() {
        when(googleClient.exchangeToken(any(Oauth2GoogleRequest.class)))
                .thenThrow(buildFeignException(400, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> authenticationService.oauth2Authentication("invalid-code"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OAUTH2_TOKEN_EXCHANGE_FAILED);

        verify(googleUserInfo, never()).getUserInfo(anyString(), anyString());
    }

    @Test
    void oauth2Authentication_whenAccessTokenMissing_throwsOauth2TokenExchangeFailed() {
        when(googleClient.exchangeToken(any(Oauth2GoogleRequest.class)))
                .thenReturn(new Oauth2GoogleResponse(null, null, 3600L, "openid email profile", "Bearer"));

        assertThatThrownBy(() -> authenticationService.oauth2Authentication("valid-code"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OAUTH2_TOKEN_EXCHANGE_FAILED);

        verify(googleUserInfo, never()).getUserInfo(anyString(), anyString());
    }

    @Test
    void oauth2Authentication_whenGoogleUserExists_reusesExistingUser() throws ParseException {
        Role userRole = Role.builder().name(PredefinedRole.ROLE_USER).build();
        Users existingUser = Users.builder()
                .id("u1")
                .userName("john.doe@gmail.com")
                .email("john.doe@gmail.com")
                .roles(Set.of(userRole))
                .build();

        GoogleUserDTO googleUser = GoogleUserDTO.builder()
                .id("google-uid-1")
                .email("john.doe@gmail.com")
                .name("John Doe")
                .givenName("John")
                .familyName("Doe")
                .build();

        when(googleClient.exchangeToken(any(Oauth2GoogleRequest.class)))
                .thenReturn(
                        new Oauth2GoogleResponse("google-access-token", null, 3600L, "openid email profile", "Bearer"));
        when(googleUserInfo.getUserInfo("json", "google-access-token")).thenReturn(googleUser);
        when(roleRepository.findByName(PredefinedRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.findByUserName("john.doe@gmail.com")).thenReturn(Optional.of(existingUser));
        when(refreshTokenService.save(existingUser))
                .thenReturn(RefreshToken.builder().token("refresh-google-1").build());

        AuthenticationResponse response = authenticationService.oauth2Authentication("valid-code");

        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getRefreshToken()).isEqualTo("refresh-google-1");
        assertThat(response.getToken()).isNotBlank();

        SignedJWT signedJWT = SignedJWT.parse(response.getToken());
        assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("john.doe@gmail.com");

        verify(userRepository).findByUserName("john.doe@gmail.com");
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void authentication_whenUserNotFound_throwsUserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest("missing", "Strong1!");
        when(userRepository.findByUserName("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authentication(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void authentication_whenWrongPassword_throwsUnauthenticated() {
        Users user = Users.builder()
                .id("u1")
                .userName("alice01")
                .password(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10).encode("Correct1!"))
                .build();
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));

        AuthenticationRequest request = new AuthenticationRequest("alice01", "Wrong1!");

        assertThatThrownBy(() -> authenticationService.authentication(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHENTICATED);
    }

    @Test
    void authentication_whenValid_returnsTokenAndRefreshToken() throws ParseException {
        Users user = Users.builder()
                .id("u1")
                .userName("alice01")
                .password(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10).encode("Strong1!"))
                .build();
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(refreshTokenService.save(user))
                .thenReturn(RefreshToken.builder().token("refresh-1").build());

        AuthenticationRequest request = new AuthenticationRequest("alice01", "Strong1!");
        AuthenticationResponse response = authenticationService.authentication(request);

        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getRefreshToken()).isEqualTo("refresh-1");
        assertThat(response.getToken()).isNotBlank();

        SignedJWT signedJWT = SignedJWT.parse(response.getToken());
        assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("alice01");
    }

    @Test
    void introspect_whenTokenValid_returnsAuthenticatedTrue() throws JOSEException, ParseException {
        Users user = Users.builder().id("u1").userName("alice01").build();
        String token = authenticationService.generateToke(user);
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);

        IntrospectResponse response = authenticationService.introSpect(
                IntrospectRequest.builder().token(token).build());

        assertThat(response.getAuthenticated()).isTrue();
    }

    @Test
    void introspect_whenTokenInvalidated_returnsAuthenticatedFalse() throws JOSEException, ParseException {
        Users user = Users.builder().id("u1").userName("alice01").build();
        String token = authenticationService.generateToke(user);
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(true);

        IntrospectResponse response = authenticationService.introSpect(
                IntrospectRequest.builder().token(token).build());

        assertThat(response.getAuthenticated()).isFalse();
    }

    @Test
    void refreshToken_whenUserIdMismatch_throwsRefreshTokenNotFound() throws JOSEException, ParseException {
        Users user = Users.builder().id("u1").userName("alice01").build();
        String accessToken = authenticationService.generateToke(user);

        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(refreshTokenService.getByToken("refresh-1"))
                .thenReturn(Optional.of(RefreshToken.builder()
                        .token("refresh-1")
                        .userId("other-user")
                        .build()));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken(accessToken);
        request.setRefreshToken("refresh-1");

        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    void refreshToken_whenValidRequest_rotatesRefreshTokenAndInvalidatesOldAccessToken()
            throws JOSEException, ParseException {
        Users user = Users.builder().id("u1").userName("alice01").build();
        String accessToken = authenticationService.generateToke(user);

        RefreshToken existing =
                RefreshToken.builder().token("refresh-1").userId("u1").build();

        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(refreshTokenService.getByToken("refresh-1")).thenReturn(Optional.of(existing));
        when(refreshTokenService.save(user))
                .thenReturn(RefreshToken.builder().token("refresh-2").build());

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken(accessToken);
        request.setRefreshToken("refresh-1");

        AuthenticationResponse response = authenticationService.refreshToken(request);

        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getRefreshToken()).isEqualTo("refresh-2");
        assertThat(response.getToken()).isNotBlank();
        verify(refreshTokenService).deleteByToken("refresh-1");

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository).save(captor.capture());
        InvalidatedToken invalidated = captor.getValue();
        assertThat(invalidated.getId()).isNotBlank();
        assertThat(invalidated.getExpiresAt()).isAfter(new Date());
    }

    private FeignException buildFeignException(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "https://oauth2.googleapis.com/token",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);

        Response response = Response.builder()
                .status(status)
                .reason("Bad Request")
                .request(request)
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();

        return FeignException.errorStatus("GoogleClient#exchangeToken", response);
    }
}
