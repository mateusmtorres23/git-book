package com.gitbook.gitbook.dtos.capitulo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConteudoCapituloRequestDTO(
        UUID idCapitulo,
        @Size(max = 150)
        String titulo,
        @Positive
        Integer ordem,
        String conteudo,
        boolean excluido
) {
    @JsonIgnore
    @AssertTrue(message = "Criação e edição exigem título, ordem e texto; exclusão exige somente o capítulo e o marcador de exclusão.")
    public boolean isAlteracaoValida() {
        if (excluido) {
            return idCapitulo != null && titulo == null && ordem == null && conteudo == null;
        }
        return titulo != null && !titulo.isBlank() && ordem != null && conteudo != null;
    }
}
