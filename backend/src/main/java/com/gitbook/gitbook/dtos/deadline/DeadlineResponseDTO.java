package com.gitbook.gitbook.dtos.deadline;

import com.gitbook.gitbook.models.Deadline;
import com.gitbook.gitbook.models.enums.StatusTask;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeadlineResponseDTO(
        UUID idDeadline,
        UUID idUsuario,
        UUID idLivro,
        String titulo,
        String descricao,
        StatusTask status,
        LocalDateTime dataLimite,
        LocalDateTime dataCriacao,
        boolean atrasada
) {
    public static DeadlineResponseDTO fromEntity(Deadline deadline) {
        boolean atrasada = deadline.getStatus() != StatusTask.CONCLUIDA
                && deadline.getDataLimite().isBefore(LocalDateTime.now());

        return new DeadlineResponseDTO(
                deadline.getIdDeadline(),
                deadline.getUsuario().getIdUsuario(),
                deadline.getLivro().getIdLivro(),
                deadline.getTitulo(),
                deadline.getDescricao(),
                deadline.getStatus(),
                deadline.getDataLimite(),
                deadline.getDataCriacao(),
                atrasada
        );
    }
}
