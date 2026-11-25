package com.gitbook.gitbook.dtos.pullrequest;

import java.util.List;
import java.util.UUID;

public record PullRequestAlteracoesResponseDTO(
        UUID idPullRequest,
        UUID idCommitOrigem,
        UUID idCommitDestinoNaAbertura,
        UUID idCommitBase,
        List<PullRequestAlteracaoCapituloResponseDTO> alteracoes
) {
}
