package com.gitbook.gitbook.dtos.auth;

import com.gitbook.gitbook.validation.SenhaNoLimiteBcrypt;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @SenhaNoLimiteBcrypt String senha
) {
    @Override
    public String toString() {
        return "LoginRequestDTO";
    }
}
