package com.okits02.SpringJWTWithOauth2.unitest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.controller.UserController;
import com.okits02.SpringJWTWithOauth2.dto.ChangePassDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.service.UserService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class, properties = "server.servlet.context-path=")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_validRequest_returnsSuccessResponse() throws Exception {
        UserRequest request = buildValidUserRequest();
        UserResponse response = buildUserResponse("alice01");
        when(userService.save(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Resister user successfully!"))
                .andExpect(jsonPath("$.result.id").value(response.getId()))
                .andExpect(jsonPath("$.result.userName").value(response.getUserName()))
                .andExpect(jsonPath("$.result.email").value(response.getEmail()))
                .andExpect(jsonPath("$.result.phone").value(response.getPhone()));

        verify(userService).save(any(UserRequest.class));
    }

    @Test
    void createUser_invalidPassword_returnsValidationErrorAndNoServiceCall() throws Exception {
        UserRequest request = buildValidUserRequest();
        request.setPassword("weakpass");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD.getCode()));

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_invalidEmail_returnsValidationErrorAndNoServiceCall() throws Exception {
        UserRequest request = buildValidUserRequest();
        request.setEmail("alice@yahoo.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_EMAIL.getCode()));

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_userExists_returnsConflict() throws Exception {
        UserRequest request = buildValidUserRequest();
        when(userService.save(any(UserRequest.class))).thenThrow(new AppException(ErrorCode.USER_EXISTS));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_EXISTS.getMessage()));

        verify(userService).save(any(UserRequest.class));
    }

    @Test
    void updateUser_validRequest_returnsUpdatedUser() throws Exception {
        UserUpdateRequest request = buildValidUserUpdateRequest();
        UserResponse response = buildUserResponse("updated01");
        String userId = UUID.randomUUID().toString();
        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(response.getId()))
                .andExpect(jsonPath("$.result.userName").value(response.getUserName()))
                .andExpect(jsonPath("$.result.email").value(response.getEmail()));

        verify(userService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void updateUser_invalidEmail_returnsValidationErrorAndNoServiceCall() throws Exception {
        UserUpdateRequest request = buildValidUserUpdateRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(put("/users/{userId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_EMAIL.getCode()));

        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_userNotFound_returnsNotFound() throws Exception {
        UserUpdateRequest request = buildValidUserUpdateRequest();
        String userId = UUID.randomUUID().toString();
        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        verify(userService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void updateMyInfo_validRequest_returnsUpdatedUser() throws Exception {
        UserInfoDTO request = buildValidUserInfo();
        UserResponse response = buildUserResponse("myself01");
        when(userService.updateMyInfo(any(UserInfoDTO.class))).thenReturn(response);

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userName").value(response.getUserName()))
                .andExpect(jsonPath("$.result.email").value(response.getEmail()));

        verify(userService).updateMyInfo(any(UserInfoDTO.class));
    }

    @Test
    void updateMyInfo_invalidPhone_returnsValidationErrorAndNoServiceCall() throws Exception {
        UserInfoDTO request = buildValidUserInfo();
        request.setPhone("0123456789");

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PHONE.getCode()));

        verifyNoInteractions(userService);
    }

    @Test
    void updateMyInfo_userNotFound_returnsNotFound() throws Exception {
        UserInfoDTO request = buildValidUserInfo();
        when(userService.updateMyInfo(any(UserInfoDTO.class))).thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        verify(userService).updateMyInfo(any(UserInfoDTO.class));
    }

    @Test
    void changePassword_validRequest_returnsChangedPasswordPayload() throws Exception {
        ChangePassDTO request = buildChangePassRequest();
        when(userService.changePassword(any(ChangePassDTO.class))).thenReturn(request);

        mockMvc.perform(put("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.oldPassword").value(request.getOldPassword()))
                .andExpect(jsonPath("$.result.newPassword").value(request.getNewPassword()));

        verify(userService).changePassword(any(ChangePassDTO.class));
    }

    @Test
    void changePassword_unauthenticated_returnsUnauthorized() throws Exception {
        ChangePassDTO request = buildChangePassRequest();
        when(userService.changePassword(any(ChangePassDTO.class)))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mockMvc.perform(put("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHENTICATED.getMessage()));

        verify(userService).changePassword(any(ChangePassDTO.class));
    }

    @Test
    void getMyInfo_success_returnsProfile() throws Exception {
        UserInfoDTO info = buildValidUserInfo();
        when(userService.getMyInfo()).thenReturn(info);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.username").value(info.getUsername()))
                .andExpect(jsonPath("$.result.email").value(info.getEmail()))
                .andExpect(jsonPath("$.result.phone").value(info.getPhone()));

        verify(userService).getMyInfo();
    }

    @Test
    void getMyInfo_userNotFound_returnsNotFound() throws Exception {
        when(userService.getMyInfo()).thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        verify(userService).getMyInfo();
    }

    @Test
    void getById_success_returnsUserById() throws Exception {
        UserResponse response = buildUserResponse("lookup01");
        String userId = response.getId();
        when(userService.getById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.userName").value(response.getUserName()));

        verify(userService).getById(userId);
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(userService.getById(userId)).thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        verify(userService).getById(userId);
    }

    @Test
    void getAll_withPagingParams_returnsPaginatedResponseContract() throws Exception {
        UserResponse u1 = buildUserResponse("paging01");
        UserResponse u2 = buildUserResponse("paging02");
        int page = 1;
        int size = 2;
        Page<UserResponse> userPage = new PageImpl<>(List.of(u1, u2), PageRequest.of(page, size), 5);
        when(userService.getAll(page, size)).thenReturn(userPage);

        mockMvc.perform(get("/users").param("page", String.valueOf(page)).param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentPage").value(1))
                .andExpect(jsonPath("$.result.totalPage").value(3))
                .andExpect(jsonPath("$.result.pageSize").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(5))
                .andExpect(jsonPath("$.result.data.length()").value(2))
                .andExpect(jsonPath("$.result.data[0].userName").value(u1.getUserName()))
                .andExpect(jsonPath("$.result.data[1].userName").value(u2.getUserName()));

        verify(userService).getAll(page, size);
    }

    @Test
    void getAll_withoutPagingParams_usesControllerDefaults() throws Exception {
        Page<UserResponse> userPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(userService.getAll(anyInt(), anyInt())).thenReturn(userPage);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentPage").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(10))
                .andExpect(jsonPath("$.result.totalElements").value(0))
                .andExpect(jsonPath("$.result.data.length()").value(0));

        verify(userService).getAll(0, 10);
    }

    @Test
    void getAll_whenServiceThrowsUnexpectedError_returnsStandardizedErrorPayload() throws Exception {
        when(userService.getAll(anyInt(), anyInt())).thenThrow(new RuntimeException("db temporary unavailable"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        verify(userService).getAll(0, 10);
    }

    private UserRequest buildValidUserRequest() {
        UserRequest request = new UserRequest();
        request.setUsername("alice01");
        request.setPassword("Strong1!");
        request.setEmail("alice01@gmail.com");
        request.setPhone("0968623160");
        request.setFirstName("Alice");
        request.setLastName("Nguyen");
        request.setDob(LocalDate.of(2000, 1, 1));
        return request;
    }

    private UserUpdateRequest buildValidUserUpdateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("update01");
        request.setPassword("Newpass1!");
        request.setEmail("update01@gmail.com");
        request.setPhone("0977777777");
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setDob(LocalDate.of(1999, 6, 15));
        request.setRoles(Set.of(RoleDTO.builder().name("USER").build()));
        return request;
    }

    private UserInfoDTO buildValidUserInfo() {
        UserInfoDTO info = new UserInfoDTO();
        info.setUsername("profile1");
        info.setEmail("profile1@gmail.com");
        info.setPhone("0988888888");
        info.setFirstName("Profile");
        info.setLastName("Owner");
        info.setDob(LocalDate.of(2001, 3, 20));
        return info;
    }

    private ChangePassDTO buildChangePassRequest() {
        return ChangePassDTO.builder()
                .oldPassword("Oldpass1!")
                .newPassword("Newpass2@")
                .build();
    }

    private UserResponse buildUserResponse(String username) {
        return UserResponse.builder()
                .id(UUID.randomUUID().toString())
                .userName(username)
                .email(username + "@gmail.com")
                .phone("0968623160")
                .firstName("First")
                .lastName("Last")
                .dob(String.valueOf(LocalDate.of(2000, 1, 1)))
                .roles(Set.of(RoleDTO.builder().name("USER").build()))
                .build();
    }
}
