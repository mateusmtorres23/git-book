package com.gitbook.gitbook.dtos;

import com.gitbook.gitbook.models.enums.StatusPullRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record PullRequestResponseDTO(
        UUID idPullRequest,
        UUID idSolicitante,
        UUID idBranchOrigem,
        UUID idBranchDestino,
        String titulo,
        String descricao,
        StatusPullRequest status,
        LocalDateTime dataAbertura
) {
}
