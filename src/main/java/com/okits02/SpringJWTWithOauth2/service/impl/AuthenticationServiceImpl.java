package com.okits02.SpringJWTWithOauth2.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.okits02.SpringJWTWithOauth2.constant.PredefinedRole;
import com.okits02.SpringJWTWithOauth2.dto.GoogleUserDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.Oauth2GoogleResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.LogoutRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.Oauth2GoogleRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;
import com.okits02.SpringJWTWithOauth2.entity.InvalidatedToken;
import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.enums.AuthProvide;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.InvalidatedTokenRepository;
import com.okits02.SpringJWTWithOauth2.repository.Oauth2.GoogleClient;
import com.okits02.SpringJWTWithOauth2.repository.Oauth2.GoogleUserInfo;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;
import com.okits02.SpringJWTWithOauth2.service.AuthenticationService;
import com.okits02.SpringJWTWithOauth2.service.RefreshTokenService;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    RefreshTokenService refreshTokenService;
    InvalidatedTokenRepository invalidatedTokenRepository;
    GoogleClient googleClient;
    GoogleUserInfo googleUserInfo;

    @NonFinal
    @Value("${app.jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${app.jwt.access-token-expiration}")
    private long JWT_ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${app.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION_IN_MS;

    @NonFinal
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String CLIENT_ID;

    @NonFinal
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String CLIENT_SECRET;

    @NonFinal
    @Value("${app.oauth2.redirect-uri}")
    private String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    @Override
    public AuthenticationResponse oauth2Authentication(String code) {
        log.info("Google code = {}", code);

        Oauth2GoogleResponse tokenResponse;
        try {
            tokenResponse = googleClient.exchangeToken(Oauth2GoogleRequest.builder()
                    .code(code)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUri(REDIRECT_URI)
                    .grantType(GRANT_TYPE)
                    .build());
        } catch (FeignException e) {
            log.warn("Google token exchange failed: {}", e.contentUTF8());
            throw new AppException(ErrorCode.OAUTH2_TOKEN_EXCHANGE_FAILED);
        }

        if (Objects.isNull(tokenResponse) || !StringUtils.hasText(tokenResponse.getAccessToken())) {
            throw new AppException(ErrorCode.OAUTH2_TOKEN_EXCHANGE_FAILED);
        }

        GoogleUserDTO googleUserResponse;
        try {
            googleUserResponse = googleUserInfo.getUserInfo("json", tokenResponse.getAccessToken());
        } catch (FeignException e) {
            log.warn("Google user info request failed: {}", e.contentUTF8());
            throw new AppException(ErrorCode.OAUTH2_USER_INFO_FAILED);
        }

        if (Objects.isNull(googleUserResponse) || !StringUtils.hasText(googleUserResponse.getEmail())) {
            throw new AppException(ErrorCode.OAUTH2_INVALID_USER_INFO);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository
                .findByName(PredefinedRole.ROLE_USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));

        var user = userRepository
                .findByUserName(googleUserResponse.getEmail())
                .orElseGet(() -> userRepository.save(Users.builder()
                        .userName(googleUserResponse.getEmail())
                        .email(googleUserResponse.getEmail())
                        .firstName(googleUserResponse.getGivenName())
                        .lastName(googleUserResponse.getFamilyName())
                        .roles(roles)
                        .authProvider(AuthProvide.GOOGLE)
                        .build()));

        String accessToken = generateToke(user);
        String refreshToken = refreshTokenService.save(user).getToken();

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introSpect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken();
        boolean isVerified = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isVerified = false;
        }
        return IntrospectResponse.builder().authenticated(isVerified).build();
    }

    @Override
    public AuthenticationResponse authentication(AuthenticationRequest authenticationRequest) {
        var user = userRepository
                .findByUserName(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!matches) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToke(user);
        var refreshToken = refreshTokenService.save(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest)
            throws ParseException, JOSEException {
        var signToken = verifyToken(refreshTokenRequest.getToken());
        var userName = signToken.getJWTClaimsSet().getSubject();
        var user =
                userRepository.findByUserName(userName).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Optional<RefreshToken> refreshToken = refreshTokenService.getByToken(refreshTokenRequest.getRefreshToken());
        if (!refreshToken.isPresent()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!refreshToken.get().getUserId().equals(user.getId())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        var jit = signToken.getJWTClaimsSet().getJWTID();
        var expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiresAt(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);
        refreshTokenService.deleteByToken(refreshToken.get().getToken());
        return AuthenticationResponse.builder()
                .token(generateToke(user))
                .refreshToken(refreshTokenService.save(user).getToken())
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        try {
            var signToken = verifyToken(logoutRequest.getToken());

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .token(logoutRequest.getToken())
                    .expiresAt(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
            refreshTokenService.deleteByToken(logoutRequest.getRefreshToken());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToke(Users users) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.subject(users.getUserName());
        builder.issuer("okits");
        builder.issueTime(new Date());
        builder.jwtID(UUID.randomUUID().toString());
        builder.expirationTime(new Date(new Date().getTime() + JWT_ACCESS_TOKEN_EXPIRATION));
        builder.claim("scope", buildScope(users));
        JWTClaimsSet jwtClaimsSet = builder.build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(Users users) {
        StringJoiner scope = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(users.getRoles())) {
            users.getRoles().forEach(role -> {
                scope.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        scope.add(permission.getName());
                    });
                }
            });
        }
        return scope.toString();
    }
}
