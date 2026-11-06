package com.gitbook.gitbook.dtos.commit;

import java.util.UUID;

public record CommitPaiResponseDTO(
        UUID idPaternidade,
        UUID idCommitFilho,
        UUID idCommitPai,
        Integer ordemPai
) {
}
