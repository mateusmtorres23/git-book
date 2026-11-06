package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;

import java.util.List;
import java.util.UUID;

public interface DeadlineService {
    DeadlineResponseDTO buscarPorId(UUID idDeadline);

    List<DeadlineResponseDTO> listarPorLivro(UUID idLivro);

    List<DeadlineResponseDTO> listarPorUsuario(UUID idUsuario);
}
