package com.gitbook.gitbook.dtos.livro;

import java.time.LocalDateTime;
import java.util.UUID;

public record LivroResponseDTO(
        UUID idLivro,
        String nomeLivro,
        String descricao,
        String versao,
        UUID idDono,
        LocalDateTime dataCriacao
) {
}
