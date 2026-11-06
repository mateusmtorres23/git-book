package com.gitbook.gitbook.dtos.pullrequest;

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
        LocalDateTime dataAbertura,
        UUID idCommitOrigem,
        UUID idCommitDestinoNaAbertura,
        UUID idCommitMerge,
        UUID idAutorDecisao,
        String justificativaRejeicao,
        LocalDateTime dataFechamento
) {
}
