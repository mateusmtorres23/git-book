package com.gitbook.gitbook.dtos.capitulo;

import java.util.UUID;

public record CapituloResponseDTO(
    UUID idCapitulo,
    UUID idLivro,
    String titulo,
    Integer ordem
) {
}
