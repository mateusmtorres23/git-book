package com.gitbook.gitbook.dtos.deadline;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeadlineUpdateRequestDTO(
        @NotNull UUID idUsuario,
        @NotBlank @Size(max = 150) String titulo,
        String descricao,
        @NotNull LocalDateTime dataLimite
) {
}
