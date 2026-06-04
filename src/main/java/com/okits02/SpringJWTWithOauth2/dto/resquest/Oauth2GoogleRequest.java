package com.okits02.SpringJWTWithOauth2.dto.resquest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Oauth2GoogleRequest {
    String code;
    String clientId;
    String clientSecret;
    String redirectUri;
    String grantType;
}
