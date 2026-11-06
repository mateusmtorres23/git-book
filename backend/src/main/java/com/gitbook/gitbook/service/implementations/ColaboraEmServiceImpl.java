package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.enums.CargoColab;
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
    @Transactional
    public ColaboraEm cadastrar(ColaboraEmRequestDTO dto) {
        return null;
    }

    @Transactional
    @Override
    public ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto) {
        if (dto.cargo() == null || dto.cargo() == CargoColab.GESTOR) {
            return null;
        }

        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).get();

        if (colaboracao.getCargo() == CargoColab.GESTOR) {
            return null;
        }

        colaboracao.setCargo(dto.cargo());

        ColaboraEm atualizada = colaboraEmRepository.save(colaboracao);

        return ColaboraEmResponseDTO.fromEntity(atualizada);
    }
}
