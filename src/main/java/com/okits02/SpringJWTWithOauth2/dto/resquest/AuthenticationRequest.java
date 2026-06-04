package com.okits02.SpringJWTWithOauth2.dto.resquest;

import com.okits02.SpringJWTWithOauth2.validator.RegexConstraint;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank
    @RegexConstraint(min = 6, max = 10, message = "INVALID_USERNAME")
    String username;

    @NotBlank
    @RegexConstraint(
            min = 8,
            max = 20,
            pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "INVALID_PASSWORD")
    String password;
}
