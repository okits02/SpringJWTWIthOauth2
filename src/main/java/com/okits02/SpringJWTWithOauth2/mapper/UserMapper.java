package com.okits02.SpringJWTWithOauth2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;
import com.okits02.SpringJWTWithOauth2.entity.Users;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "username", target = "userName")
    Users toUser(UserRequest userCreationRequest);

    UserResponse toUserResponse(Users user);

    @Mapping(source = "userName", target = "username")
    UserInfoDTO toUserInfoDTO(Users user);

    @Mapping(target = "roles", ignore = true)
    void updateUsers(@MappingTarget Users users, UserUpdateRequest request);

    void updateMyInfo(@MappingTarget Users users, UserInfoDTO request);
}
