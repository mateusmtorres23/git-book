package com.gitbook.gitbook.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommitResponseDTO(
        UUID idCommit,
        UUID idLivro,
        UUID idAutor,
        UUID idBranch,
        String mensagem,
        LocalDateTime dataCriacao
) {
}
