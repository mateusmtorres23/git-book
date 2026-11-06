package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.sugestao.SugestaoRequestDTO;
import com.gitbook.gitbook.dtos.sugestao.SugestaoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SugestaoService {
    public SugestaoResponseDTO adicionarSugestao(UUID idPullRequest, SugestaoRequestDTO dados, UUID idUsuario);
    public SugestaoResponseDTO buscarPorId(UUID idSugestao, UUID idUsuario);
    public List<SugestaoResponseDTO> listarSugestoes(UUID idPullRequest, UUID idUsuario);
    public SugestaoResponseDTO retirarSugestao(UUID idSugestao, UUID idUsuario);
}
