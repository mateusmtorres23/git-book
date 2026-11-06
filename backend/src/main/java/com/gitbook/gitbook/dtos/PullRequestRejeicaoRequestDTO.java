package com.gitbook.gitbook.dtos;

import jakarta.validation.constraints.NotBlank;

public record PullRequestRejeicaoRequestDTO(
        @NotBlank
        String justificativa
) {
}
