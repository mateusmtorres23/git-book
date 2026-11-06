package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.commit.CommitDetalheResponseDTO;
import com.gitbook.gitbook.dtos.commit.CommitRequestDTO;
import com.gitbook.gitbook.dtos.commit.CommitResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.Commit;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.repository.CommitRepository;
import com.gitbook.gitbook.service.CommitService;
import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.ValidacaoService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import com.gitbook.gitbook.service.support.VersionamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommitServiceImpl implements CommitService {
    private final CommitRepository commitRepository;
    private final AcessoLivroService acesso;
    private final VersionamentoService versionamento;
    private final ValidacaoService validacao;

    @Override
    @Transactional
    public CommitDetalheResponseDTO criar(UUID idBranch, CommitRequestDTO dados, UUID idUsuario) {
        validacao.validar(dados);
        Livro livro = acesso.bloquearLivro(versionamento.livroDaBranch(idBranch));
        Branch branch = versionamento.obterBranch(idBranch);
        acesso.exigirEscrita(livro, branch, idUsuario);
        Commit anterior = branch.getCommitAtual();
        if (!Objects.equals(VersionamentoMapper.id(anterior), dados.idCommitBase())) {
            throw new ConflitoVersionamentoException("A branch mudou desde a edição. Atualize a base antes de confirmar.");
        }
        var base = versionamento.reconstruir(anterior, livro.getIdLivro());
        var alteracoes = versionamento.planejarAlteracoes(base, dados.alteracoes());
        Commit novo = versionamento.gravar(livro, branch, acesso.obterUsuario(idUsuario), dados.mensagem(),
                alteracoes, anterior, null);
        return versionamento.detalhe(novo);
    }

    @Override
    public CommitDetalheResponseDTO buscarPorId(UUID idCommit, UUID idUsuario) {
        Commit commit = versionamento.obterCommit(idCommit);
        acesso.exigirLeitura(acesso.obterLivro(commit.getIdLivro()), idUsuario);
        return versionamento.detalhe(commit);
    }

    @Override
    public List<CommitResponseDTO> listarPorLivro(UUID idLivro, UUID idUsuario) {
        acesso.exigirLeitura(acesso.obterLivro(idLivro), idUsuario);
        return commitRepository.findByIdLivroOrderByDataCriacaoDesc(idLivro).stream()
                .map(VersionamentoMapper::commit).toList();
    }

    @Override
    public List<CommitResponseDTO> listarPorBranch(UUID idBranch, UUID idUsuario) {
        Branch branch = versionamento.obterBranch(idBranch);
        acesso.exigirLeitura(branch.getLivro(), idUsuario);
        return versionamento.historico(branch.getCommitAtual()).stream().map(VersionamentoMapper::commit).toList();
    }

    @Override
    public VersaoLivroResponseDTO consultarVersao(UUID idCommit, UUID idUsuario) {
        Commit commit = versionamento.obterCommit(idCommit);
        acesso.exigirLeitura(acesso.obterLivro(commit.getIdLivro()), idUsuario);
        return versionamento.versao(commit.getIdLivro(), commit);
    }
}
