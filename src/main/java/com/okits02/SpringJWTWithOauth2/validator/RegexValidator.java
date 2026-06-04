package com.okits02.SpringJWTWithOauth2.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegexValidator implements ConstraintValidator<RegexConstraint, String> {
    private Pattern pattern;
    private int max;
    private int min;

    @Override
    public void initialize(RegexConstraint constraintAnnotation) {
        String regex = constraintAnnotation.pattern();
        this.pattern = Pattern.compile((regex == null || regex.isBlank()) ? ".*" : regex);
        this.max = constraintAnnotation.max();
        this.min = constraintAnnotation.min();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }

        if (s.length() < min || s.length() > max) {
            return false;
        }

        return pattern.matcher(s).matches();
    }
}
