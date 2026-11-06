package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.commit.CommitPaiResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommitPaiService {
    CommitPaiResponseDTO buscarPorId(UUID idPaternidade, UUID idUsuario);
    List<CommitPaiResponseDTO> listarPais(UUID idCommitFilho, UUID idUsuario);
    Optional<CommitPaiResponseDTO> buscarPaiPrincipal(UUID idCommitFilho, UUID idUsuario);
    List<CommitPaiResponseDTO> listarFilhos(UUID idCommitPai, UUID idUsuario);
}
