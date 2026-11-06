package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DeadlineService {
    DeadlineResponseDTO buscarPorId(UUID idDeadline);

    List<DeadlineResponseDTO> listarPorLivro(UUID idLivro);

    List<DeadlineResponseDTO> listarPorUsuario(UUID idUsuario);

    DeadlineResponseDTO cadastrar(DeadlineRequestDTO dto);

    DeadlineResponseDTO atualizar(UUID idDeadline, DeadlineUpdateRequestDTO dto);
}
