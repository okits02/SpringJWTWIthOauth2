package com.okits02.SpringJWTWithOauth2.dto.resquest;

import java.time.LocalDate;

import com.okits02.SpringJWTWithOauth2.validator.RegexConstraint;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    @NotBlank
    @RegexConstraint(min = 6, message = "INVALID_USERNAME")
    String username;

    @NotBlank
    @RegexConstraint(
            min = 8,
            max = 20,
            pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "INVALID_PASSWORD")
    String password;

    @NotBlank
    @RegexConstraint(pattern = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "INVALID_EMAIL")
    String email;

    @NotBlank
    @RegexConstraint(min = 9, max = 10, pattern = "^(03|05|07|08|09)\\d+$", message = "INVALID_PHONE")
    String phone;

    String firstName;
    String lastName;
    LocalDate dob;
}
