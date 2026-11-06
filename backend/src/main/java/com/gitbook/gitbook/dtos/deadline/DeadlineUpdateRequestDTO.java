package com.gitbook.gitbook.dtos.deadline;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeadlineUpdateRequestDTO(
        UUID idUsuario,
        String titulo,
        String descricao,
        LocalDateTime dataLimite
) {
}
