package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ColaboraEmService {
    ColaboraEmResponseDTO buscarPorId(UUID idColaboracao, UUID idSolicitante);
    List<ColaboraEmResponseDTO> listarPorLivro(UUID idLivro, UUID idSolicitante);
    List<ColaboraEmResponseDTO> listarPorUsuario(UUID idUsuario, UUID idSolicitante);
    ColaboraEmResponseDTO cadastrar(ColaboraEmRequestDTO dto, UUID idSolicitante);
    ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto, UUID idSolicitante);
    boolean remover(UUID idColaboracao, UUID idSolicitante);
}
