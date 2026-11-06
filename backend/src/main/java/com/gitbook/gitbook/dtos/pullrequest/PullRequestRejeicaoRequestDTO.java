package com.gitbook.gitbook.dtos.pullrequest;

import jakarta.validation.constraints.NotBlank;

public record PullRequestRejeicaoRequestDTO(
        @NotBlank
        String justificativa
) {
}
