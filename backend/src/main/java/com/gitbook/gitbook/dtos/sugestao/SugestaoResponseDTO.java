package com.gitbook.gitbook.dtos.sugestao;

import com.gitbook.gitbook.models.enums.StatusSugestao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SugestaoResponseDTO(
        UUID idSugestao,
        UUID idCapitulo,
        UUID idUsuario,
        UUID idPullRequest,
        String categoria,
        String trechoOriginal,
        String trechoSugerido,
        String justificativa,
        Integer posicaoInicial,
        Integer posicaoFim,
        StatusSugestao status,
        LocalDateTime dataSugestao,
        LocalDateTime dataAvaliacao
) {
}
