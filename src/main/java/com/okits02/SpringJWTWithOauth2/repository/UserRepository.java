package com.okits02.SpringJWTWithOauth2.repository;

import com.okits02.SpringJWTWithOauth2.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByUsername(String username);
    Optional<Users> findByUsername(String username);
}
