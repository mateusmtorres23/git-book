package com.gitbook.gitbook.dtos.colaboraEm;

import com.gitbook.gitbook.models.enums.CargoColab;

import java.util.UUID;

public record ColaboraEmRequestDTO(
        UUID idUsuario,
        UUID idLivro,
        CargoColab cargo
) {
}
