package com.gitbook.gitbook.dtos.commit;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;

import java.util.List;

public record CommitDetalheResponseDTO(
        CommitResponseDTO commit,
        List<CommitPaiResponseDTO> pais,
        List<ConteudoCapituloResponseDTO> alteracoes
) {
}
