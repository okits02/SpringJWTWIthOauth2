package com.okits02.SpringJWTWithOauth2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.okits02.SpringJWTWithOauth2.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
