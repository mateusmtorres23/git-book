package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ConteudoCapituloService {

    ConteudoCapituloResponseDTO buscarPorId(UUID idVersao);

    List<ConteudoCapituloResponseDTO> listarPorCapitulo(UUID idCapitulo);

    List<ConteudoCapituloResponseDTO> listarPorCommit(UUID idCommit);
}
