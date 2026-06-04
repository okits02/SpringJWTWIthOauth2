package com.okits02.SpringJWTWithOauth2.dto.response;

import java.util.Set;

import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    Boolean authenticated;
    String token;
    String refreshToken;
    Set<RoleDTO> role;
}
