package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRejeicaoRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestResponseDTO;
import com.gitbook.gitbook.models.enums.StatusPullRequest;

import java.util.List;
import java.util.UUID;

public interface PullRequestService {
    PullRequestResponseDTO criar(PullRequestRequestDTO dados, UUID idUsuario);
    PullRequestResponseDTO buscarPorId(UUID idPullRequest, UUID idUsuario);
    List<PullRequestResponseDTO> listarPorLivro(UUID idLivro, StatusPullRequest status, UUID idUsuario);
    VersaoLivroResponseDTO consultarConteudoProposto(UUID idPullRequest, UUID idUsuario);
    PullRequestResponseDTO aprovar(UUID idPullRequest, UUID idUsuario);
    PullRequestResponseDTO rejeitar(UUID idPullRequest, PullRequestRejeicaoRequestDTO dados, UUID idUsuario);
}
