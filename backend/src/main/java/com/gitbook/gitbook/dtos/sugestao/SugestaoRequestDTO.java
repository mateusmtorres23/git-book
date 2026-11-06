package com.gitbook.gitbook.dtos.sugestao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SugestaoRequestDTO(
        @NotNull
        UUID idCapitulo,
        @NotBlank @Size(max = 50)
        String categoria,
        @NotBlank
        String trechoOriginal,
        @NotBlank
        String trechoSugerido,
        @NotBlank
        String justificativa,
        @NotNull @PositiveOrZero
        Integer posicaoInicial,
        @NotNull @Positive
        Integer posicaoFim
) {
}
