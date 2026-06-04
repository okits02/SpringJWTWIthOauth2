package com.okits02.SpringJWTWithOauth2.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.okits02.SpringJWTWithOauth2.dto.ChangePassDTO;
import com.okits02.SpringJWTWithOauth2.dto.RoleDTO;
import com.okits02.SpringJWTWithOauth2.dto.UserInfoDTO;
import com.okits02.SpringJWTWithOauth2.dto.resquest.AuthenticationRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserRequest;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserUpdateRequest;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_validRequest_persistsDataInContainerizedMySql() throws Exception {
        UserRequest request = new UserRequest(
                "integ01",
                "Strong@123",
                "integ01@gmail.com",
                "0912345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Resister user successfully!"))
                .andExpect(jsonPath("$.result.userName").value(request.getUsername()))
                .andExpect(jsonPath("$.result.email").value(request.getEmail()))
                .andExpect(jsonPath("$.result.phone").value(request.getPhone()));

        Users savedUser = userRepository.findByUserName(request.getUsername()).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getPhone()).isEqualTo(request.getPhone());
        assertThat(savedUser.getPassword()).isNotEqualTo(request.getPassword());
    }

    @Test
    void createUser_inValidUserNameRequest_returnsValidationErrorAndDoesNotPersistData() throws Exception {
        UserRequest request = new UserRequest(
                "integ",
                "Strong@123",
                "integ01@gmail.com",
                "0912345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_USERNAME.getCode()))
                .andExpect(jsonPath("$.message").value("Username must have at lest 6 character"))
                .andExpect(jsonPath("$.result").value(nullValue()));

        assertThat(userRepository.findByUserName(request.getUsername())).isEmpty();
    }

    @Test
    void createUser_inValidPasswordRequest_returnsValidationErrorAndDoesNotPersistData() throws Exception {
        UserRequest request = new UserRequest(
                "integ02",
                "strong@123",
                "integ01@gmail.com",
                "0912345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD.getCode()))
                .andExpect(jsonPath("$.message")
                        .value("Password must have at least one uppercase characters and at lest 8 character"))
                .andExpect(jsonPath("$.result").value(nullValue()));

        assertThat(userRepository.findByUserName(request.getUsername())).isEmpty();
    }

    @Test
    void createUser_inValidGmailRequest_returnsValidationErrorAndDoesNotPersistData() throws Exception {
        UserRequest request = new UserRequest(
                "integ03",
                "Strong@123",
                "integ03@yahoo.com",
                "0912345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_EMAIL.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.result").value(nullValue()));

        assertThat(userRepository.findByUserName(request.getUsername())).isEmpty();
    }

    @Test
    void createUser_inValidPhoneRequest_returnsValidationErrorAndDoesNotPersistData() throws Exception {
        UserRequest request = new UserRequest(
                "integ04",
                "Strong@123",
                "integ01@gmail.com",
                "0112345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PHONE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PHONE.getMessage()))
                .andExpect(jsonPath("$.result").value(nullValue()));

        assertThat(userRepository.findByUserName(request.getUsername())).isEmpty();
    }

    @Test
    void updateUser_validRequest_updatesPersistedDataInContainerizedMySql() throws Exception {
        String userName = nextUsername("uup");
        String password = "Strong@123";

        createUserAndAuthenticate(userName, password);
        String accessToken = authenticateAndGetAccessToken(userName, password);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername(userName);
        updateRequest.setPassword("Updated1!");
        updateRequest.setEmail(userName + ".updated@gmail.com");
        updateRequest.setPhone("0977777777");
        updateRequest.setFirstName("Integration");
        updateRequest.setLastName("AfterUpdate");
        updateRequest.setDob(LocalDate.of(2001, 2, 2));
        updateRequest.setRoles(Set.of(RoleDTO.builder().name("USER").build()));

        mockMvc.perform(put("/indentity/users/{userId}", userName)
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.userName").value(userName))
                .andExpect(jsonPath("$.result.email").value(updateRequest.getEmail()))
                .andExpect(jsonPath("$.result.phone").value(updateRequest.getPhone()))
                .andExpect(jsonPath("$.result.firstName").value(updateRequest.getFirstName()))
                .andExpect(jsonPath("$.result.lastName").value(updateRequest.getLastName()));

        Users updatedUser = userRepository.findByUserName(userName).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo(updateRequest.getEmail());
        assertThat(updatedUser.getPhone()).isEqualTo(updateRequest.getPhone());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateRequest.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(updateRequest.getLastName());
    }

    @Test
    void updateUser_userNotFound_returnsNotFound() throws Exception {
        String userName = nextUsername("uup");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("missing01");
        updateRequest.setPassword("Updated1!");
        updateRequest.setEmail("missing01@gmail.com");
        updateRequest.setPhone("0977777777");
        updateRequest.setFirstName("Missing");
        updateRequest.setLastName("User");
        updateRequest.setDob(LocalDate.of(2001, 2, 2));
        updateRequest.setRoles(Set.of(RoleDTO.builder().name("USER").build()));

        mockMvc.perform(put("/indentity/users/{userId}", "missing01")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    void updateMyInfo_validRequest_updatesCurrentUserInContainerizedMySql() throws Exception {
        String userName = nextUsername("umy");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        UserInfoDTO updateMyInfoRequest = new UserInfoDTO();
        updateMyInfoRequest.setUsername(userName);
        updateMyInfoRequest.setEmail(userName + ".updated@gmail.com");
        updateMyInfoRequest.setPhone("0988888888");
        updateMyInfoRequest.setFirstName("Integration");
        updateMyInfoRequest.setLastName("AfterMyInfoUpdate");
        updateMyInfoRequest.setDob(LocalDate.of(2002, 3, 3));

        mockMvc.perform(put("/indentity/users/me")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(updateMyInfoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.userName").value(userName))
                .andExpect(jsonPath("$.result.email").value(updateMyInfoRequest.getEmail()))
                .andExpect(jsonPath("$.result.phone").value(updateMyInfoRequest.getPhone()))
                .andExpect(jsonPath("$.result.firstName").value(updateMyInfoRequest.getFirstName()))
                .andExpect(jsonPath("$.result.lastName").value(updateMyInfoRequest.getLastName()));

        Users updatedUser = userRepository.findByUserName(userName).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo(updateMyInfoRequest.getEmail());
        assertThat(updatedUser.getPhone()).isEqualTo(updateMyInfoRequest.getPhone());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateMyInfoRequest.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(updateMyInfoRequest.getLastName());
        assertThat(updatedUser.getDob()).isEqualTo(updateMyInfoRequest.getDob());
    }

    @Test
    void updateMyInfo_invalidPhone_returnsValidationError() throws Exception {
        String userName = nextUsername("umy");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        UserInfoDTO request = new UserInfoDTO();
        request.setUsername(userName);
        request.setEmail(userName + "@gmail.com");
        request.setPhone("0111111111");

        mockMvc.perform(put("/indentity/users/me")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PHONE.getCode()));
    }

    @Test
    void changePassword_validRequest_returnsNullResultContract() throws Exception {
        String userName = nextUsername("ucp");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        ChangePassDTO request = ChangePassDTO.builder()
                .oldPassword(password)
                .newPassword("Newpass2@")
                .build();

        mockMvc.perform(put("/indentity/users/change-password")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

    @Test
    void getMyInfo_success_returnsAuthenticatedUserProfile() throws Exception {
        String userName = nextUsername("umi");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        mockMvc.perform(get("/indentity/users/me")
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.username").value(userName))
                .andExpect(jsonPath("$.result.email").value(userName + "@gmail.com"));
    }

    @Test
    void getById_success_returnsUserByDatabaseId() throws Exception {
        String userName = nextUsername("ugb");
        String password = "Strong@123";
        String accessToken = createUserAndAuthenticate(userName, password);

        Users savedUser = userRepository.findByUserName(userName).orElseThrow();

        mockMvc.perform(get("/indentity/users/{userId}", savedUser.getId())
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.result.userName").value(userName));
    }

    @Test
    void getAll_success_containsCreatedUsers() throws Exception {
        String userName01 = nextUsername("ugl");
        String userName02 = nextUsername("ugl");
        String password = "Strong@123";

        String accessToken = createUserAndAuthenticate(userName01, password);

        UserRequest secondUserRequest = new UserRequest(
                userName02,
                password,
                userName02 + "@gmail.com",
                "0912345678",
                "Second",
                "User",
                LocalDate.of(2000, 1, 1));
        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUserRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/indentity/users")
                        .contextPath("/indentity")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.data[*].userName", hasItem(userName01)))
                .andExpect(jsonPath("$.result.data[*].userName", hasItem(userName02)));
    }

    private String createUserAndAuthenticate(String userName, String password) throws Exception {
        UserRequest createRequest = new UserRequest(
                userName,
                password,
                userName + "@gmail.com",
                "0912345678",
                "Integration",
                "Tester",
                LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/indentity/users")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        return authenticateAndGetAccessToken(userName, password);
    }

    private String authenticateAndGetAccessToken(String userName, String password) throws Exception {
        AuthenticationRequest loginRequest = new AuthenticationRequest(userName, password);
        String loginResponse = mockMvc.perform(post("/indentity/auth/login")
                        .contextPath("/indentity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).at("/result/token").asText();
    }

    private String nextUsername(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 7);
        return (prefix + suffix).substring(0, 10);
    }
}
