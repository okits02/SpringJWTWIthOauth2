package com.okits02.SpringJWTWithOauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.okits02.SpringJWTWithOauth2.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByName(String name);

    Optional<Role> findByName(String roleName);
}
