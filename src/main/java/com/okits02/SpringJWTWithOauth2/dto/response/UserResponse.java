package com.okits02.SpringJWTWithOauth2.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;
    String email;
    String firstName;
    String lastName;
    String bob;
}
