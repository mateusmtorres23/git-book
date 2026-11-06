package com.gitbook.gitbook.dtos.deadline;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeadlineRequestDTO(
        UUID idUsuario,
        UUID idLivro,
        String titulo,
        String descricao,
        LocalDateTime dataLimite
) {
}
