package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.service.ColaboraEmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColaboraEmServiceImpl implements ColaboraEmService {

    private final ColaboraEmRepository colaboraEmRepository;

    @Override
    @Transactional(readOnly = true)
    public ColaboraEm buscarPorId(UUID idColaboracao) {
        return colaboraEmRepository.findById(idColaboracao).get();
    }

    @Override
    public List<ColaboraEm> listarPorLivro(UUID idLivro) {
        return colaboraEmRepository.findByLivro_IdLivro(idLivro);
    }

    @Override
    public List<ColaboraEm> listarPorUsuario(UUID idUsuario) {
        return colaboraEmRepository.findByUsuario_IdUsuario(idUsuario);
    }
}
