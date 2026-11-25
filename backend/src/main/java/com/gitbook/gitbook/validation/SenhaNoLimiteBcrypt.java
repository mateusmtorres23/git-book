package com.gitbook.gitbook.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SenhaNoLimiteBcryptValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaNoLimiteBcrypt {
    String message() default "A senha deve ter no máximo 72 bytes em UTF-8";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
