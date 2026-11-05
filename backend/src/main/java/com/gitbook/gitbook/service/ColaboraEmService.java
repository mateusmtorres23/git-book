package com.gitbook.gitbook.service;

import com.gitbook.gitbook.models.ColaboraEm;

import java.util.UUID;

public interface ColaboraEmService {
    public ColaboraEm buscarPorId(UUID idColaboracao);
}
