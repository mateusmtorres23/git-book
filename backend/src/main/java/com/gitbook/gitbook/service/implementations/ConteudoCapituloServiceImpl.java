package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;
import com.gitbook.gitbook.models.Capitulo;
import com.gitbook.gitbook.models.Commit;
import com.gitbook.gitbook.models.ConteudoCapitulo;
import com.gitbook.gitbook.repository.CapituloRepository;
import com.gitbook.gitbook.repository.ConteudoCapituloRepository;
import com.gitbook.gitbook.service.ConteudoCapituloService;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import com.gitbook.gitbook.service.support.VersionamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConteudoCapituloServiceImpl implements ConteudoCapituloService {

    private final ConteudoCapituloRepository conteudoCapituloRepository;
    private final CapituloRepository capituloRepository;
    private final VersionamentoService versionamento;
    private final AcessoLivroService acesso;

    @Override
    @Transactional(readOnly = true)
    public ConteudoCapituloResponseDTO buscarPorId(UUID idVersao, UUID idSolicitante) {
        ConteudoCapitulo conteudo = conteudoCapituloRepository.findById(idVersao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Versão de capítulo não encontrada."));
        acesso.exigirLeitura(conteudo.getCapitulo().getLivro(), idSolicitante);
        return VersionamentoMapper.conteudo(conteudo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteudoCapituloResponseDTO> listarPorCapitulo(UUID idCapitulo, UUID idSolicitante) {
        Capitulo capitulo = capituloRepository.findById(idCapitulo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Capítulo não encontrado."));
        acesso.exigirLeitura(capitulo.getLivro(), idSolicitante);
        return conteudoCapituloRepository.findByCapitulo_IdCapitulo(idCapitulo)
                .stream()
                .map(VersionamentoMapper::conteudo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteudoCapituloResponseDTO> listarPorCommit(UUID idCommit, UUID idSolicitante) {
        Commit commit = versionamento.obterCommit(idCommit);
        acesso.exigirLeitura(acesso.obterLivro(commit.getIdLivro()), idSolicitante);
        return conteudoCapituloRepository
                .findByCommit_IdCommitOrderByOrdemAsc(idCommit)
                .stream()
                .map(VersionamentoMapper::conteudo)
                .toList();
    }
}
