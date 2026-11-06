package com.gitbook.gitbook.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BranchRequestDTO(
        @NotBlank @Size(max = 100)
        String nome,
        UUID idCommitOrigem
) {
}
