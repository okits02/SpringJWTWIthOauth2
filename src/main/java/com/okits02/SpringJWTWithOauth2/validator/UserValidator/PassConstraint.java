package com.okits02.SpringJWTWithOauth2.validator.UserValidator;

import java.lang.annotation.*;

import com.nimbusds.jose.Payload;

import jakarta.validation.Constraint;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface PassConstraint {
    String message() default "Invalid password number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min();

    int max();
}
