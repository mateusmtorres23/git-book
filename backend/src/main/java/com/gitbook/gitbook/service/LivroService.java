package com.gitbook.gitbook.service;

import com.gitbook.gitbook.dtos.livro.LivroRequestDTO;
import com.gitbook.gitbook.dtos.livro.LivroResponseDTO;

import java.util.List;
import java.util.UUID;

public interface LivroService {
    public LivroResponseDTO criarLivro(UUID idUsuario, LivroRequestDTO livro);
    public LivroResponseDTO buscarLivro(UUID idLivro, UUID idUsuario);
    public List<LivroResponseDTO> listarLivros(UUID idUsuario);
    public LivroResponseDTO atualizarLivro(UUID idLivro, LivroRequestDTO livro, UUID idUsuario);
}
