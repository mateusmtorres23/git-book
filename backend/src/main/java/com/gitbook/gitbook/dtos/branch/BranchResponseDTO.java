package com.gitbook.gitbook.dtos.branch;

import java.time.LocalDateTime;
import java.util.UUID;

public record BranchResponseDTO(
        UUID idBranch,
        UUID idLivro,
        UUID idAutor,
        String nome,
        boolean main,
        UUID idCommitAtual,
        LocalDateTime dataCriacao
) {
}
