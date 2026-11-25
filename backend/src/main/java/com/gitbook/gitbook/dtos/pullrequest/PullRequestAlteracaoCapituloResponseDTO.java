package com.gitbook.gitbook.dtos.pullrequest;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;

import java.util.UUID;

public record PullRequestAlteracaoCapituloResponseDTO(
        UUID idCapitulo,
        ConteudoCapituloResponseDTO antes,
        ConteudoCapituloResponseDTO depois
) {
}
