package com.gitbook.gitbook.dtos.colaboraEm;

import jakarta.validation.constraints.NotNull;

import com.gitbook.gitbook.models.enums.CargoColab;

public record ColaboraEmUpdateRequestDTO(@NotNull CargoColab cargo) {
}
