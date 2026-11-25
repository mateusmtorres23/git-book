package com.gitbook.gitbook.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class SenhaNoLimiteBcryptValidator implements ConstraintValidator<SenhaNoLimiteBcrypt, String> {
    private static final int LIMITE_BYTES = 72;

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= LIMITE_BYTES;
    }
}
