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
public class UserResponse {
    String id;
    String userName;
    String email;
    String phone;
    String firstName;
    String lastName;
    String dob;
    Set<RoleDTO> roles;
}
