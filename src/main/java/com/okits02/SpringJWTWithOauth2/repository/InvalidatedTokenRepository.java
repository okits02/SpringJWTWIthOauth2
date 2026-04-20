package com.okits02.SpringJWTWithOauth2.repository;

import com.okits02.SpringJWTWithOauth2.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
}
