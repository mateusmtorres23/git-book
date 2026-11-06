package com.gitbook.gitbook.dtos.capitulo;

import java.util.UUID;

public record ConteudoCapituloResponseDTO(
        UUID idVersao,
        UUID idCommit,
        UUID idCapitulo,
        String titulo,
        Integer ordem,
        String conteudo,
        boolean excluido
) {
}
