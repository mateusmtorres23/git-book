package com.gitbook.gitbook.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CapituloRequestDTO(
        @NotBlank @Size(max = 150)
        String titulo,
        @NotNull @Positive
        Integer ordem
) {
}
