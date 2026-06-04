package com.okits02.SpringJWTWithOauth2.service.impl;

import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.okits02.SpringJWTWithOauth2.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse save(UserRequest request) throws AppException {
        if (userRepository.findByUserName(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        Users newUser = userMapper.toUser(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.ROLE_USER).ifPresent(roles::add);
        newUser.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(newUser));
    }

    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest request) throws AppException {
        Users users =
                userRepository.findByUserName(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUsers(users, request);
        var roles = roleRepository.findAllById(
                request.getRoles().stream().map(RoleDTO::getName).toList());
        users.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(users));
    }

    @Override
    public UserResponse updateMyInfo(UserInfoDTO request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Users users = userRepository.findByUserName(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateMyInfo(users, request);
        return userMapper.toUserResponse(userRepository.save(users));
    }

    @Override
    public ChangePassDTO changePassword(ChangePassDTO request) throws AppException {
        return null;
    }

    @Override
    public UserInfoDTO getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Users users = userRepository.findByUserName(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserInfoDTO(users);
    }

    @Override
    public UserResponse getById(String userId) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(users);
    }

    @Override
    public Page<UserResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserResponse);
    }
}
