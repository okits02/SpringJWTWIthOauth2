package com.okits02.SpringJWTWithOauth2.service;

import java.util.Optional;

import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;
import com.okits02.SpringJWTWithOauth2.entity.Users;

public interface RefreshTokenService {
    public Optional<RefreshToken> getByToken(String token);

    public void deleteByToken(String token);

    public void deleteByUserId(String userId);

    public RefreshToken save(Users user);
}
