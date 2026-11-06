package com.gitbook.gitbook.dtos;

import java.util.List;
import java.util.UUID;

public record VersaoLivroResponseDTO(
        UUID idLivro,
        UUID idCommit,
        List<ConteudoCapituloResponseDTO> capitulos
) {
}
