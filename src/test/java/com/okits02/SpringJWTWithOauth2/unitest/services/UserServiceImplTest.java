package com.okits02.SpringJWTWithOauth2.unitest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.okits02.SpringJWTWithOauth2.constant.PredefinedRole;
import com.okits02.SpringJWTWithOauth2.dto.ChangePassDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;
import com.okits02.SpringJWTWithOauth2.entity.Role;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.UserMapper;
import com.okits02.SpringJWTWithOauth2.repository.RoleRepository;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;
import com.okits02.SpringJWTWithOauth2.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_whenUserExists_throwsUserExists() {
        UserRequest request = buildUserRequest("alice01", "Strong1!");
        when(userRepository.findByUserName("alice01"))
                .thenReturn(Optional.of(Users.builder().build()));

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_EXISTS);
    }

    @Test
    void save_whenValidRequest_savesEncodedPasswordAndDefaultRole() {
        UserRequest request = buildUserRequest("alice01", "Strong1!");
        Users newUser = Users.builder().userName("alice01").build();
        Role userRole = Role.builder().name(PredefinedRole.ROLE_USER).build();
        UserResponse expected = UserResponse.builder().userName("alice01").build();

        when(userRepository.findByUserName("alice01")).thenReturn(Optional.empty());
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(passwordEncoder.encode("Strong1!")).thenReturn("encoded-password");
        when(roleRepository.findById(PredefinedRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(Users.class))).thenReturn(expected);

        UserResponse result = userService.save(request);

        assertThat(result).isEqualTo(expected);
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getRoles()).contains(userRole);
    }

    @Test
    void updateUser_whenUserNotFound_throwsUserNotFound() {
        UserUpdateRequest request = buildUserUpdateRequest("alice01");
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("alice01", request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updateUser_whenUserExists_updatesRolesAndReturnsResponse() {
        UserUpdateRequest request = buildUserUpdateRequest("alice01");
        Users user = Users.builder().id("u1").userName("alice01").build();
        Role adminRole = Role.builder().name("ADMIN").build();
        UserResponse expected = UserResponse.builder().userName("alice01").build();

        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(List.of("ADMIN"))).thenReturn(List.of(adminRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.updateUser("alice01", request);

        assertThat(result).isEqualTo(expected);
        verify(userMapper).updateUsers(user, request);
        assertThat(user.getRoles()).containsExactly(adminRole);
    }

    @Test
    void updateMyInfo_whenUserNotFound_throwsUserNotFound() {
        setAuthentication("alice01");
        UserInfoDTO infoDTO = buildUserInfoDTO("alice01");
        when(userRepository.findByUserName("alice01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMyInfo(infoDTO))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updateMyInfo_whenUserExists_updatesAndReturnsResponse() {
        setAuthentication("alice01");
        UserInfoDTO infoDTO = buildUserInfoDTO("alice01");
        Users user = Users.builder().id("u1").userName("alice01").build();
        UserResponse expected = UserResponse.builder().userName("alice01").build();

        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.updateMyInfo(infoDTO);

        assertThat(result).isEqualTo(expected);
        verify(userMapper).updateMyInfo(user, infoDTO);
    }

    @Test
    void getMyInfo_whenUserExists_returnsMappedInfo() {
        setAuthentication("alice01");
        Users user = Users.builder().id("u1").userName("alice01").build();
        UserInfoDTO expected = buildUserInfoDTO("alice01");

        when(userRepository.findByUserName("alice01")).thenReturn(Optional.of(user));
        when(userMapper.toUserInfoDTO(user)).thenReturn(expected);

        UserInfoDTO result = userService.getMyInfo();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getById_whenNotFound_throwsUserNotFound() {
        when(userRepository.findById("u404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById("u404"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getById_whenFound_returnsMappedResponse() {
        Users user = Users.builder().id("u1").userName("alice01").build();
        UserResponse expected = UserResponse.builder().userName("alice01").build();

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.getById("u1");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_returnsMappedPageContent() {
        Users u1 = Users.builder().id("u1").userName("alice01").build();
        Users u2 = Users.builder().id("u2").userName("bob02").build();
        Page<Users> userPage = new PageImpl<>(List.of(u1, u2), PageRequest.of(0, 2), 2);

        when(userRepository.findAll(eq(PageRequest.of(0, 2)))).thenReturn(userPage);
        when(userMapper.toUserResponse(u1))
                .thenReturn(UserResponse.builder().userName("alice01").build());
        when(userMapper.toUserResponse(u2))
                .thenReturn(UserResponse.builder().userName("bob02").build());

        Page<UserResponse> result = userService.getAll(0, 2);

        assertThat(result.getContent()).extracting(UserResponse::getUserName).containsExactly("alice01", "bob02");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void changePassword_currentImplementationReturnsNull() {
        ChangePassDTO request =
                ChangePassDTO.builder().oldPassword("old").newPassword("new").build();

        ChangePassDTO result = userService.changePassword(request);

        assertThat(result).isNull();
    }

    private void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, null));
        SecurityContextHolder.setContext(context);
    }

    private UserRequest buildUserRequest(String username, String password) {
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(username + "@gmail.com");
        request.setPhone("0968623160");
        return request;
    }

    private UserUpdateRequest buildUserUpdateRequest(String username) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(username);
        request.setPassword("Strong1!");
        request.setEmail(username + "@gmail.com");
        request.setPhone("0968623160");
        request.setRoles(Set.of(RoleDTO.builder().name("ADMIN").build()));
        return request;
    }

    private UserInfoDTO buildUserInfoDTO(String username) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUsername(username);
        dto.setEmail(username + "@gmail.com");
        dto.setPhone("0968623160");
        return dto;
    }
}
