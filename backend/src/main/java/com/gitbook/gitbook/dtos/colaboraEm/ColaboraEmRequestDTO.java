package com.gitbook.gitbook.dtos.colaboraEm;

import jakarta.validation.constraints.NotNull;

import com.gitbook.gitbook.models.enums.CargoColab;

import java.util.UUID;

public record ColaboraEmRequestDTO(
        @NotNull UUID idUsuario,
        @NotNull UUID idLivro,
        @NotNull CargoColab cargo
) {
}
