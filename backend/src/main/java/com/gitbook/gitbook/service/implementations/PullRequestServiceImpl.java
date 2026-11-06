package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRejeicaoRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestRequestDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestResponseDTO;
import com.gitbook.gitbook.models.*;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import com.gitbook.gitbook.models.enums.StatusSugestao;
import com.gitbook.gitbook.repository.PullRequestRepository;
import com.gitbook.gitbook.repository.SugestaoRepository;
import com.gitbook.gitbook.service.PullRequestService;
import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.ValidacaoService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import com.gitbook.gitbook.service.support.VersionamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PullRequestServiceImpl implements PullRequestService {
    private final PullRequestRepository repository;
    private final SugestaoRepository sugestaoRepository;
    private final AcessoLivroService acesso;
    private final VersionamentoService versionamento;
    private final ValidacaoService validacao;

    @Override
    @Transactional
    public PullRequestResponseDTO criar(PullRequestRequestDTO dados, UUID idUsuario) {
        validacao.validar(dados);
        Livro livro = acesso.bloquearLivro(versionamento.livroDaBranch(dados.idBranchOrigem()));
        Branch origem = versionamento.obterBranch(dados.idBranchOrigem());
        acesso.exigirEscrita(livro, origem, idUsuario);
        Branch destino = versionamento.obterBranch(dados.idBranchDestino());
        validarBranches(livro, origem, destino);
        if (origem.getCommitAtual() == null) {
            throw new IllegalArgumentException("A origem ainda não possui commits.");
        }
        Commit base = versionamento.baseComum(origem.getCommitAtual(), destino.getCommitAtual(), livro.getIdLivro());
        var estadoOrigem = versionamento.reconstruir(origem.getCommitAtual(), livro.getIdLivro());
        var estadoDestino = versionamento.reconstruir(destino.getCommitAtual(), livro.getIdLivro());
        var estadoBase = versionamento.reconstruir(base, livro.getIdLivro());
        if (versionamento.mesmosConteudos(estadoOrigem, estadoBase)
                || versionamento.mesmosConteudos(estadoOrigem, estadoDestino)) {
            throw new IllegalArgumentException("Não há diferenças elegíveis para abrir o Pull Request.");
        }
        PullRequest pr = new PullRequest(acesso.obterUsuario(idUsuario), origem, destino, dados.titulo(), dados.descricao());
        return VersionamentoMapper.pullRequest(repository.saveAndFlush(pr));
    }

    @Override
    public PullRequestResponseDTO buscarPorId(UUID idPullRequest, UUID idUsuario) {
        PullRequest pr = obter(idPullRequest);
        autorizarLeitura(pr, idUsuario);
        return VersionamentoMapper.pullRequest(pr);
    }

    @Override
    public List<PullRequestResponseDTO> listarPorLivro(UUID idLivro, StatusPullRequest status, UUID idUsuario) {
        acesso.exigirLeitura(acesso.obterLivro(idLivro), idUsuario);
        return repository.findByLivroId(idLivro).stream()
                .filter(pr -> status == null || pr.getStatus() == status)
                .map(VersionamentoMapper::pullRequest).toList();
    }

    @Override
    public VersaoLivroResponseDTO consultarConteudoProposto(UUID idPullRequest, UUID idUsuario) {
        PullRequest pr = obter(idPullRequest);
        UUID idLivro = autorizarLeitura(pr, idUsuario);
        exigirOrigemFixada(pr);
        return versionamento.versao(idLivro, pr.getCommitOrigem());
    }

    @Override
    @Transactional
    public PullRequestResponseDTO aprovar(UUID idPullRequest, UUID idUsuario) {
        Livro livro = acesso.bloquearLivro(livroDoPullRequest(idPullRequest));
        acesso.exigirDecisaoPullRequest(livro, idUsuario);
        PullRequest pr = obter(idPullRequest);
        exigirAberto(pr);
        exigirOrigemFixada(pr);
        if (sugestaoRepository.existsByPullRequestAndStatus(pr, StatusSugestao.PENDENTE)) {
            throw new ConflitoVersionamentoException("O Pull Request possui sugestões pendentes.");
        }
        Branch destino = versionamento.obterBranch(pr.getIdBranchDestino());
        if (!destino.isMain() || !livro.getIdLivro().equals(destino.getLivro().getIdLivro())) {
            throw new IllegalStateException("O destino do Pull Request não é a main do livro.");
        }
        Commit anterior = destino.getCommitAtual();
        Commit origem = pr.getCommitOrigem();
        Commit base = versionamento.baseComum(origem, anterior, livro.getIdLivro());
        var estadoOrigem = versionamento.reconstruir(origem, livro.getIdLivro());
        var estadoDestino = versionamento.reconstruir(anterior, livro.getIdLivro());
        var resultado = versionamento.combinar(versionamento.reconstruir(base, livro.getIdLivro()),
                estadoOrigem, estadoDestino);
        if (versionamento.mesmosConteudos(estadoDestino, resultado)) {
            throw new ConflitoVersionamentoException("As alterações da proposta já estão integradas ou não produzem diferenças.");
        }
        // Em destino vazio, a origem fornece o único pai e o estado herdado.
        var alteracoes = versionamento.diferencas(anterior == null ? estadoOrigem : estadoDestino, resultado);
        Commit merge = versionamento.gravar(livro, destino, acesso.obterUsuario(idUsuario),
                "Integra Pull Request " + pr.getIdPullRequest(), alteracoes,
                anterior == null ? origem : anterior, anterior == null ? null : origem);
        pr.aprovar(acesso.obterUsuario(idUsuario), merge);
        return VersionamentoMapper.pullRequest(repository.saveAndFlush(pr));
    }

    @Override
    @Transactional
    public PullRequestResponseDTO rejeitar(UUID idPullRequest, PullRequestRejeicaoRequestDTO dados, UUID idUsuario) {
        validacao.validar(dados);
        Livro livro = acesso.bloquearLivro(livroDoPullRequest(idPullRequest));
        acesso.exigirDecisaoPullRequest(livro, idUsuario);
        PullRequest pr = obter(idPullRequest);
        exigirAberto(pr);
        pr.rejeitar(acesso.obterUsuario(idUsuario), dados.justificativa());
        return VersionamentoMapper.pullRequest(repository.saveAndFlush(pr));
    }

    private PullRequest obter(UUID idPullRequest) {
        return repository.findById(idPullRequest)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pull Request não encontrado."));
    }

    private UUID livroDoPullRequest(UUID idPullRequest) {
        return repository.findLivroIdByPullRequest(idPullRequest)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pull Request não encontrado."));
    }

    private UUID autorizarLeitura(PullRequest pr, UUID idUsuario) {
        UUID idLivro = livroDoPullRequest(pr.getIdPullRequest());
        acesso.exigirLeitura(acesso.obterLivro(idLivro), idUsuario);
        return idLivro;
    }

    private void validarBranches(Livro livro, Branch origem, Branch destino) {
        if (origem.isMain() || !destino.isMain() || origem.getIdBranch().equals(destino.getIdBranch())
                || !livro.getIdLivro().equals(destino.getLivro().getIdLivro())) {
            throw new IllegalArgumentException("O PR deve sair de uma branch de trabalho para a main do mesmo livro.");
        }
    }

    private void exigirAberto(PullRequest pr) {
        if (pr.getStatus() != StatusPullRequest.ABERTA) {
            throw new ConflitoVersionamentoException("O Pull Request já foi encerrado.");
        }
    }

    private void exigirOrigemFixada(PullRequest pr) {
        if (pr.getCommitOrigem() == null) {
            throw new ConflitoVersionamentoException("O Pull Request não possui uma versão de origem fixada; abra uma nova proposta.");
        }
    }
}
