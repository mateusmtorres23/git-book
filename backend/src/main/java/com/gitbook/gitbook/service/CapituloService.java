package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.capitulo.CapituloResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CapituloService {
    public List<CapituloResponseDTO> buscarPorBranch(UUID idBranch, UUID idUsuario);
    public CapituloResponseDTO buscarCapitulo(UUID idCapitulo, UUID idUsuario, UUID idBranch);
}
