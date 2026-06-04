package com.okits02.SpringJWTWithOauth2.dto;

import java.time.LocalDate;

import com.okits02.SpringJWTWithOauth2.validator.RegexConstraint;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoDTO {
    @RegexConstraint(min = 6, max = 10, message = "INVALID_USERNAME")
    String username;

    @RegexConstraint(pattern = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "INVALID_EMAIL")
    String email;

    @RegexConstraint(min = 9, max = 10, pattern = "^(03|05|07|08|09)\\d+$", message = "INVALID_PHONE")
    String phone;

    String firstName;
    String lastName;
    LocalDate dob;
}
