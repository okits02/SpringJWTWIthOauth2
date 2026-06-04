package com.okits02.SpringJWTWithOauth2.validator.UserValidator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<PhoneConstraint, String> {
    private int min;
    private int max;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    @Override
    public void initialize(PhoneConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s.length() < min || s.length() > max) {
            return false;
        }
        return PHONE_PATTERN.matcher(s).matches();
    }
}
