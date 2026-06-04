package com.okits02.SpringJWTWithOauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.okits02.SpringJWTWithOauth2.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);

    Optional<Permission> findByName(String name);
}
