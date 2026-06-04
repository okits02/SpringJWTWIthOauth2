package com.okits02.SpringJWTWithOauth2.service;

import java.text.ParseException;

import com.nimbusds.jose.*;
import com.okits02.SpringJWTWithOauth2.dto.response.AuthenticationResponse;
import com.okits02.SpringJWTWithOauth2.dto.response.IntrospectResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.IntrospectRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.LogoutRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.RefreshTokenRequest;

public interface AuthenticationService {
    public AuthenticationResponse oauth2Authentication(String code);

    public IntrospectResponse introSpect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;

    public AuthenticationResponse authentication(AuthenticationRequest authenticationRequest);

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest)
            throws ParseException, JOSEException;

    public void logout(LogoutRequest logoutRequest);
}
