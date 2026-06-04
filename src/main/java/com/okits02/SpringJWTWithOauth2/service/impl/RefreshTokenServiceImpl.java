package com.okits02.SpringJWTWithOauth2.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.RefreshTokenRepository;
import com.okits02.SpringJWTWithOauth2.service.RefreshTokenService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {
    RefreshTokenRepository refreshTokenRepository;

    @NonFinal
    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationInMs;

    @Override
    public Optional<RefreshToken> getByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
        return Optional.of(refreshToken);
    }

    @Override
    public void deleteByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
        refreshTokenRepository.delete(refreshToken);
    }

    @Override
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public RefreshToken save(Users user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        Instant expiryDate = Instant.now().plusMillis(refreshTokenExpirationInMs);
        Long ttlSeconds = refreshTokenExpirationInMs / 1000;
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userEmail(user.getEmail())
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiryTime(expiryDate)
                .timeToLive(ttlSeconds)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
