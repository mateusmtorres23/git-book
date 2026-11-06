package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.commit.CommitDetalheResponseDTO;
import com.gitbook.gitbook.dtos.commit.CommitRequestDTO;
import com.gitbook.gitbook.dtos.commit.CommitResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CommitService {
    CommitDetalheResponseDTO criar(UUID idBranch, CommitRequestDTO dados, UUID idUsuario);
    CommitDetalheResponseDTO buscarPorId(UUID idCommit, UUID idUsuario);
    List<CommitResponseDTO> listarPorLivro(UUID idLivro, UUID idUsuario);
    List<CommitResponseDTO> listarPorBranch(UUID idBranch, UUID idUsuario);
    VersaoLivroResponseDTO consultarVersao(UUID idCommit, UUID idUsuario);
}
