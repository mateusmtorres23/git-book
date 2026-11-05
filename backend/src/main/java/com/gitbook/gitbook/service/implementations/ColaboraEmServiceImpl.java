package com.gitbook.gitbook.service;

import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColaboraEmService {

    private final ColaboraEmRepository colaboraEmRepository;

    @Transactional(readOnly = true)
    public ColaboraEm buscarPorId(UUID idColaboracao) {
        return colaboraEmRepository.findById(idColaboracao)

    }
}
