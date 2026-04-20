package com.okits02.SpringJWTWithOauth2.dto.resquest;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectRequest {
    String token;
}
