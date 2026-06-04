package com.okits02.SpringJWTWithOauth2.dto;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleDTO {
    String name;
    Set<PermissionDTO> permissions;
}
