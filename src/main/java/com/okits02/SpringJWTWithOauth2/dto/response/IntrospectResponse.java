package com.okits02.SpringJWTWithOauth2.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectResponse {
    Boolean valid;
    String role;
}
