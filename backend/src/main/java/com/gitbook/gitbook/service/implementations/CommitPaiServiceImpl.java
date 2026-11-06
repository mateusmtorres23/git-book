package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.commit.CommitPaiResponseDTO;
import com.gitbook.gitbook.models.Commit;
import com.gitbook.gitbook.models.CommitPai;
import com.gitbook.gitbook.repository.CommitPaiRepository;
import com.gitbook.gitbook.service.CommitPaiService;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import com.gitbook.gitbook.service.support.VersionamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommitPaiServiceImpl implements CommitPaiService {
    private final CommitPaiRepository repository;
    private final AcessoLivroService acesso;
    private final VersionamentoService versionamento;

    @Override
    public CommitPaiResponseDTO buscarPorId(UUID idPaternidade, UUID idUsuario) {
        CommitPai vinculo = repository.findById(idPaternidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relação de paternidade não encontrada."));
        autorizar(vinculo.getCommitFilho(), idUsuario);
        validarLivro(vinculo);
        return VersionamentoMapper.pai(vinculo);
    }

    @Override
    public List<CommitPaiResponseDTO> listarPais(UUID idCommitFilho, UUID idUsuario) {
        Commit filho = versionamento.obterCommit(idCommitFilho);
        autorizar(filho, idUsuario);
        return versionamento.pais(filho).stream().map(VersionamentoMapper::pai).toList();
    }

    @Override
    public Optional<CommitPaiResponseDTO> buscarPaiPrincipal(UUID idCommitFilho, UUID idUsuario) {
        return listarPais(idCommitFilho, idUsuario).stream().filter(pai -> pai.ordemPai() == 1).findFirst();
    }

    @Override
    public List<CommitPaiResponseDTO> listarFilhos(UUID idCommitPai, UUID idUsuario) {
        autorizar(versionamento.obterCommit(idCommitPai), idUsuario);
        return repository.findByCommitPai_IdCommit(idCommitPai).stream()
                .peek(this::validarLivro).map(VersionamentoMapper::pai).toList();
    }

    private void autorizar(Commit commit, UUID idUsuario) {
        acesso.exigirLeitura(acesso.obterLivro(commit.getIdLivro()), idUsuario);
    }

    private void validarLivro(CommitPai vinculo) {
        if (!vinculo.getCommitFilho().getIdLivro().equals(vinculo.getCommitPai().getIdLivro())) {
            throw new IllegalStateException("Relação de ancestralidade entre livros diferentes.");
        }
    }
}
