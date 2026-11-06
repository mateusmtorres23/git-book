package com.gitbook.gitbook.dtos.colaboraEm;

import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.enums.CargoColab;

import java.util.UUID;

public record ColaboraEmResponseDTO(
        UUID idColaboracao,
        UUID idUsuario,
        UUID idLivro,
        CargoColab cargo
) {
    public static ColaboraEmResponseDTO fromEntity(ColaboraEm colaboracao) {
        return new ColaboraEmResponseDTO(
                colaboracao.getIdColaboracao(),
                colaboracao.getUsuario().getIdUsuario(),
                colaboracao.getLivro().getIdLivro(),
                colaboracao.getCargo()
        );
    }
}
