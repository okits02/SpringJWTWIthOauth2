package com.okits02.SpringJWTWithOauth2.entity;

import java.time.Instant;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refresh_token")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {
    @Id
    String id;

    String token;
    String userId;
    String userEmail;
    Instant expiryTime;
    Long timeToLive;
}
