package com.okits02.SpringJWTWithOauth2.mapper;

import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserCreationRequest;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreationRequest userCreationRequest);
    UserResponse toUserResponse(Users user);
}
