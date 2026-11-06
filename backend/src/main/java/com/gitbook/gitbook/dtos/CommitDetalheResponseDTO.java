package com.gitbook.gitbook.dtos;

import java.util.List;

public record CommitDetalheResponseDTO(
        CommitResponseDTO commit,
        List<CommitPaiResponseDTO> pais,
        List<ConteudoCapituloResponseDTO> alteracoes
) {
}
