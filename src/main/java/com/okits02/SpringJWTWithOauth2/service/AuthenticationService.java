package com.okits02.SpringJWTWithOauth2.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;


    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    public IntrospectResponse introSpect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken();
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verify = signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verify && expiryTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authentication(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!matches){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToke(authenticationRequest.getUsername());
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public String generateToke(String username){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.subject(username);
        builder.issuer("okits");
        builder.issueTime(new Date());
        builder.expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()));
        builder.claim("userId", "custom");
        JWTClaimsSet jwtClaimsSet = builder.build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
