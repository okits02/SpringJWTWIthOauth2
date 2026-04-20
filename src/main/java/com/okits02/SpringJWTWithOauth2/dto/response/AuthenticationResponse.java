package com.okits02.SpringJWTWithOauth2.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    String refreshToken;
    String role;
    Boolean authenticated;
}
