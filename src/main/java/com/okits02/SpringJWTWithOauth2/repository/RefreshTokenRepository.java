package com.okits02.SpringJWTWithOauth2.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);

    boolean existsByToken(String token);
}
