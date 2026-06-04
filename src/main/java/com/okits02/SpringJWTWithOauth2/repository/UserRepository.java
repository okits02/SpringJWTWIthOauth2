package com.okits02.SpringJWTWithOauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.okits02.SpringJWTWithOauth2.entity.Users;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByUserName(String username);

    Optional<Users> findByUserName(String username);
}
