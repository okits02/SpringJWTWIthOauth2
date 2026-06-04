package com.okits02.SpringJWTWithOauth2.service;

import org.springframework.data.domain.Page;

import com.okits02.SpringJWTWithOauth2.dto.ChangePassDTO;
import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;

public interface UserService {
    public UserResponse save(UserRequest request);

    public UserResponse updateUser(String userId, UserUpdateRequest request);

    public UserResponse updateMyInfo(UserInfoDTO request);

    public UserInfoDTO getMyInfo();

    public ChangePassDTO changePassword(ChangePassDTO request);

    public UserResponse getById(String userId);

    public Page<UserResponse> getAll(int page, int size);
}
