package com.gitbook.gitbook.dtos.pullrequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PullRequestRequestDTO(
        @NotNull
        UUID idBranchOrigem,
        @NotNull
        UUID idBranchDestino,
        @NotBlank @Size(max = 150)
        String titulo,
        String descricao
) {
    @JsonIgnore
    @AssertTrue(message = "As branches de origem e destino devem ser diferentes.")
    public boolean isBranchesDistintas() {
        return idBranchOrigem == null || idBranchDestino == null || !idBranchOrigem.equals(idBranchDestino);
    }
}
