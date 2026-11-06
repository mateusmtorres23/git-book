package com.gitbook.gitbook.service;

import com.gitbook.gitbook.models.ColaboraEm;

import java.util.List;
import java.util.UUID;

public interface ColaboraEmService {
    public ColaboraEm buscarPorId(UUID idColaboracao);
    public List<ColaboraEm> listarPorLivro(UUID idLivro);
    public List<ColaboraEm> listarPorUsuario(UUID idUsuario);
}
