package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineStatusRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DeadlineService {
    DeadlineResponseDTO buscarPorId(UUID idDeadline, UUID idSolicitante);

    List<DeadlineResponseDTO> listarPorLivro(UUID idLivro, UUID idSolicitante);

    List<DeadlineResponseDTO> listarPorUsuario(UUID idUsuario, UUID idSolicitante);

    DeadlineResponseDTO cadastrar(DeadlineRequestDTO dto, UUID idSolicitante);

    DeadlineResponseDTO atualizar(UUID idDeadline, DeadlineUpdateRequestDTO dto, UUID idSolicitante);

    DeadlineResponseDTO atualizarStatus(UUID idDeadline, DeadlineStatusRequestDTO dto, UUID idSolicitante);

    boolean remover(UUID idDeadline, UUID idSolicitante);
}
