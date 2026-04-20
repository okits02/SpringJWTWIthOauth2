package com.okits02.SpringJWTWithOauth2.repository;

import com.okits02.SpringJWTWithOauth2.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
