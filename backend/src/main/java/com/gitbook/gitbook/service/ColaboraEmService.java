package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.models.ColaboraEm;

import java.util.List;
import java.util.UUID;

public interface ColaboraEmService {
    public ColaboraEmResponseDTO buscarPorId(UUID idColaboracao);
    public List<ColaboraEmResponseDTO> listarPorLivro(UUID idLivro);
    public List<ColaboraEmResponseDTO> listarPorUsuario(UUID idUsuario);
    public ColaboraEmResponseDTO cadastrar(ColaboraEmRequestDTO dto);
    public ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto);
}
