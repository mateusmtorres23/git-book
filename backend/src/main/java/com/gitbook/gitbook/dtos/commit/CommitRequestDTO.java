package com.gitbook.gitbook.dtos.commit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CommitRequestDTO(
        @NotBlank @Size(max = 255)
        String mensagem,
        UUID idCommitBase,
        @NotEmpty
        List<@NotNull @Valid ConteudoCapituloRequestDTO> alteracoes
) {
    @JsonIgnore
    @AssertTrue(message = "Envie apenas uma alteração por capítulo e posições distintas entre os capítulos criados ou editados.")
    public boolean isAlteracoesSemDuplicidade() {
        if (alteracoes == null) {
            return true;
        }
        Set<UUID> capitulos = new HashSet<>();
        Set<Integer> ordens = new HashSet<>();
        for (ConteudoCapituloRequestDTO alteracao : alteracoes) {
            if (alteracao == null) {
                continue;
            }
            if (alteracao.idCapitulo() != null && !capitulos.add(alteracao.idCapitulo())) {
                return false;
            }
            if (!alteracao.excluido() && alteracao.ordem() != null && !ordens.add(alteracao.ordem())) {
                return false;
            }
        }
        return true;
    }
}
