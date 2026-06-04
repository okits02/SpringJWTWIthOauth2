package com.okits02.SpringJWTWithOauth2.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {RegexValidator.class})
public @interface RegexConstraint {
    String message() default "{jakarta.validation.constraints.Max.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max() default Integer.MAX_VALUE;

    int min() default Integer.MIN_VALUE;

    String pattern() default ".*";
}
