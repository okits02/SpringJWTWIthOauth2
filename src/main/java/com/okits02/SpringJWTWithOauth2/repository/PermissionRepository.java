package com.okits02.SpringJWTWithOauth2.repository;

import com.okits02.SpringJWTWithOauth2.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
