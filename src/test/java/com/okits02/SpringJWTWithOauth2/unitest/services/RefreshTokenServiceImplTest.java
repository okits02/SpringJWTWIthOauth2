package com.okits02.SpringJWTWithOauth2.unitest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.okits02.SpringJWTWithOauth2.entity.RefreshToken;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.RefreshTokenRepository;
import com.okits02.SpringJWTWithOauth2.service.impl.RefreshTokenServiceImpl;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationInMs", 60000L);
    }

    @Test
    void getByToken_whenTokenNotFound_throwsTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.getByToken("missing-token"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOKEN_NOT_FOUND);
    }

    @Test
    void getByToken_whenExists_returnsToken() {
        RefreshToken token = buildRefreshToken("user-1", "refresh-1");
        when(refreshTokenRepository.findByToken("refresh-1")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.getByToken("refresh-1");

        assertThat(result).contains(token);
    }

    @Test
    void deleteByToken_whenTokenNotFound_throwsTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.deleteByToken("missing-token"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOKEN_NOT_FOUND);
    }

    @Test
    void deleteByToken_whenTokenExists_deletesToken() {
        RefreshToken token = buildRefreshToken("user-1", "refresh-1");
        when(refreshTokenRepository.findByToken("refresh-1")).thenReturn(Optional.of(token));

        refreshTokenService.deleteByToken("refresh-1");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByUserId_deletesAllTokensForUser() {
        refreshTokenService.deleteByUserId("user-1");

        verify(refreshTokenRepository).deleteByUserId("user-1");
    }

    @Test
    void save_generatesTokenAndTtlFromConfiguredExpiration() {
        Users user = Users.builder().id("user-1").email("user1@gmail.com").build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        RefreshToken saved = refreshTokenService.save(user);
        Instant after = Instant.now();

        verify(refreshTokenRepository).deleteByUserId("user-1");
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getUserEmail()).isEqualTo("user1@gmail.com");
        assertThat(saved.getTimeToLive()).isEqualTo(60L);
        assertThat(saved.getExpiryTime()).isAfterOrEqualTo(before.plusMillis(60000L));
        assertThat(saved.getExpiryTime())
                .isBeforeOrEqualTo(after.plusMillis(60000L).plusSeconds(1));
    }

    private RefreshToken buildRefreshToken(String userId, String token) {
        return RefreshToken.builder()
                .id("rt-1")
                .token(token)
                .userId(userId)
                .userEmail(userId + "@gmail.com")
                .timeToLive(60L)
                .expiryTime(Instant.now().plusSeconds(60))
                .build();
    }
}
