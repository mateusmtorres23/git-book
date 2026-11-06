package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;
import com.gitbook.gitbook.models.ConteudoCapitulo;
import com.gitbook.gitbook.repository.ConteudoCapituloRepository;
import com.gitbook.gitbook.service.ConteudoCapituloService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConteudoCapituloServiceImpl implements ConteudoCapituloService {

    private final ConteudoCapituloRepository conteudoCapituloRepository;

    @Override
    @Transactional(readOnly = true)
    public ConteudoCapituloResponseDTO buscarPorId(UUID idVersao) {
        ConteudoCapitulo conteudo =
                conteudoCapituloRepository.findById(idVersao).get();

        return VersionamentoMapper.conteudo(conteudo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteudoCapituloResponseDTO> listarPorCapitulo(UUID idCapitulo) {
        return conteudoCapituloRepository.findByCapitulo_IdCapitulo(idCapitulo)
                .stream()
                .map(VersionamentoMapper::conteudo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteudoCapituloResponseDTO> listarPorCommit(UUID idCommit) {
        return conteudoCapituloRepository
                .findByCommit_IdCommitOrderByOrdemAsc(idCommit)
                .stream()
                .map(VersionamentoMapper::conteudo)
                .toList();
    }
}
