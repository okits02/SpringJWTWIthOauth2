package com.okits02.SpringJWTWithOauth2.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePassDTO {
    String oldPassword;
    String newPassword;
}
