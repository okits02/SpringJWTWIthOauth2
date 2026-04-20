package com.okits02.SpringJWTWithOauth2.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.okits02.SpringJWTWithOauth2.dto.response.UserResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.UserCreationRequest;
import com.okits02.SpringJWTWithOauth2.entity.Users;
import com.okits02.SpringJWTWithOauth2.exception.AppException;
import com.okits02.SpringJWTWithOauth2.exception.ErrorCode;
import com.okits02.SpringJWTWithOauth2.mapper.UserMapper;
import com.okits02.SpringJWTWithOauth2.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public UserResponse save(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        Users user = new Users();
        user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

}
