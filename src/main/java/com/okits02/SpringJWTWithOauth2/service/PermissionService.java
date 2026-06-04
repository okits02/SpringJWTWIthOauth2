package com.okits02.SpringJWTWithOauth2.service;

import java.util.List;

import com.okits02.SpringJWTWithOauth2.dto.PermissionDTO;

public interface PermissionService {
    public PermissionDTO save(PermissionDTO request);

    public void delete(PermissionDTO request);

    public void deleteAll();

    public PermissionDTO update(PermissionDTO request);

    public List<PermissionDTO> findAll();

    public PermissionDTO findByName(String Name);
}
