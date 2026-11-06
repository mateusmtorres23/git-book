package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.branch.BranchRequestDTO;
import com.gitbook.gitbook.dtos.branch.BranchResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchResponseDTO criarMain(UUID idLivro, UUID idUsuario);
    BranchResponseDTO criar(UUID idLivro, BranchRequestDTO dados, UUID idUsuario);
    BranchResponseDTO buscarPorId(UUID idBranch, UUID idUsuario);
    List<BranchResponseDTO> listarPorLivro(UUID idLivro, UUID idUsuario);
    VersaoLivroResponseDTO consultarConteudo(UUID idBranch, UUID idUsuario);
}
