package com.gitbook.gitbook.dtos.livro;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;

import java.util.List;
import java.util.UUID;

public record VersaoLivroResponseDTO(
        UUID idLivro,
        UUID idCommit,
        List<ConteudoCapituloResponseDTO> capitulos
) {
}
