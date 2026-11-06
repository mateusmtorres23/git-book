package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.ColaboraEmResponseDTO;
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
    public ColaboraEmResponseDTO buscarPorId(UUID idColaboracao) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).get();
        return ColaboraEmResponseDTO.fromEntity(colaboracao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorLivro(UUID idLivro) {
        return colaboraEmRepository.findByLivro_IdLivro(idLivro)
                .stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorUsuario(UUID idUsuario) {
        return colaboraEmRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public ColaboraEm cadastrar(ColaboraEmRequestDTO dto) {
        return null;
    }

}
