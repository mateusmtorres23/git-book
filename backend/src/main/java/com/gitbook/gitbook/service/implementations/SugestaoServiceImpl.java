package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.sugestao.SugestaoRequestDTO;
import com.gitbook.gitbook.dtos.sugestao.SugestaoResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.PullRequest;
import com.gitbook.gitbook.models.Sugestao;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import com.gitbook.gitbook.models.enums.StatusSugestao;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.PullRequestRepository;
import com.gitbook.gitbook.repository.SugestaoRepository;
import com.gitbook.gitbook.service.SugestaoService;
import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.VersionamentoService;
import com.gitbook.gitbook.service.support.VersionamentoService.EstadoCapitulo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SugestaoServiceImpl implements SugestaoService {
    private final SugestaoRepository sugestaoRepository;
    private final PullRequestRepository pullRequestRepository;
    private final LivroRepository livroRepository;
    private final BranchRepository branchRepository;
    private final AcessoLivroService acesso;
    private final ColaboraEmRepository colaboraEmRepository;
    private final VersionamentoService versionamento;

    @Override
    @Transactional
    public SugestaoResponseDTO adicionarSugestao(UUID idPullRequest, SugestaoRequestDTO dados, UUID idUsuario) {
        if (idPullRequest == null || !dadosValidos(dados)) {
            throw new IllegalArgumentException("Os dados da sugestão são inválidos.");
        }
        Usuario usuario = acesso.obterUsuario(idUsuario);
        Livro livro = bloquearLivroDoPullRequest(idPullRequest, "Pull Request não encontrado.");
        exigirVisibilidade(livro, idUsuario, "Pull Request não encontrado.");
        exigirRevisor(livro, idUsuario);

        PullRequest pullRequest = obterPullRequest(idPullRequest);
        exigirAberto(pullRequest);
        if (pullRequest.getCommitOrigem() == null) {
            throw new ConflitoVersionamentoException("O Pull Request não possui uma versão de origem fixada.");
        }
        if (!livro.getIdLivro().equals(pullRequest.getCommitOrigem().getIdLivro())) {
            throw new IllegalStateException("O commit de origem não pertence ao livro do Pull Request.");
        }

        // Usa o commit fixado no PR, mesmo que a branch já tenha novos commits.
        EstadoCapitulo capitulo = versionamento.reconstruir(
                pullRequest.getCommitOrigem(), livro.getIdLivro()).get(dados.idCapitulo());
        if (capitulo == null || dados.posicaoFim() > capitulo.conteudo().length()) {
            throw new IllegalArgumentException("A seleção não pertence ao conteúdo proposto.");
        }
        String trecho = capitulo.conteudo().substring(dados.posicaoInicial(), dados.posicaoFim());
        if (!trecho.equals(dados.trechoOriginal())) {
            throw new IllegalArgumentException("O trecho informado difere do conteúdo proposto.");
        }

        Sugestao sugestao = new Sugestao();
        sugestao.setCapitulo(capitulo.capitulo());
        sugestao.setUsuario(usuario);
        sugestao.setPullRequest(pullRequest);
        sugestao.setCategoria(dados.categoria());
        sugestao.setTrechoOriginal(dados.trechoOriginal());
        sugestao.setTrechoSugerido(dados.trechoSugerido());
        sugestao.setJustificativa(dados.justificativa());
        sugestao.setPosicaoInicial(dados.posicaoInicial());
        sugestao.setPosicaoFim(dados.posicaoFim());
        sugestao.setStatus(StatusSugestao.PENDENTE);

        return montarResposta(sugestaoRepository.saveAndFlush(sugestao));
    }

    @Override
    public SugestaoResponseDTO buscarPorId(UUID idSugestao, UUID idUsuario) {
        acesso.obterUsuario(idUsuario);
        Sugestao sugestao = obterSugestao(idSugestao);
        exigirVisibilidade(obterLivro(sugestao.getPullRequest()), idUsuario, "Sugestão não encontrada.");
        return montarResposta(sugestao);
    }

    @Override
    public List<SugestaoResponseDTO> listarSugestoes(UUID idPullRequest, UUID idUsuario) {
        acesso.obterUsuario(idUsuario);
        PullRequest pullRequest = obterPullRequest(idPullRequest);
        exigirVisibilidade(obterLivro(pullRequest), idUsuario, "Pull Request não encontrado.");

        List<SugestaoResponseDTO> respostas = new ArrayList<>();
        for (Sugestao sugestao : sugestaoRepository.findByPullRequest(pullRequest)) {
            respostas.add(montarResposta(sugestao));
        }
        return respostas;
    }

    @Override
    @Transactional
    public SugestaoResponseDTO retirarSugestao(UUID idSugestao, UUID idUsuario) {
        acesso.obterUsuario(idUsuario);
        UUID idPullRequest = sugestaoRepository.findPullRequestIdBySugestao(idSugestao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão não encontrada."));
        Livro livro = bloquearLivroDoPullRequest(idPullRequest, "Sugestão não encontrada.");
        exigirVisibilidade(livro, idUsuario, "Sugestão não encontrada.");
        exigirRevisor(livro, idUsuario);

        // Carrega a sugestão após o lock para enxergar a última retirada confirmada.
        Sugestao sugestao = obterSugestao(idSugestao);
        if (!sugestao.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new AccessDeniedException("Somente o autor pode retirar a sugestão.");
        }
        exigirAberto(sugestao.getPullRequest());
        if (sugestao.getStatus() == StatusSugestao.RETIRADA) {
            return montarResposta(sugestao);
        }

        sugestao.setStatus(StatusSugestao.RETIRADA);
        sugestao.setDataAvaliacao(LocalDateTime.now());
        return montarResposta(sugestaoRepository.saveAndFlush(sugestao));
    }

    private Sugestao obterSugestao(UUID idSugestao) {
        return sugestaoRepository.findById(idSugestao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão não encontrada."));
    }

    private PullRequest obterPullRequest(UUID idPullRequest) {
        return pullRequestRepository.findById(idPullRequest)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pull Request não encontrado."));
    }

    private Livro obterLivro(PullRequest pullRequest) {
        return branchRepository.findById(pullRequest.getIdBranchDestino())
                .map(Branch::getLivro).orElse(null);
    }

    private Livro bloquearLivroDoPullRequest(UUID idPullRequest, String mensagemIndisponivel) {
        UUID idLivro = pullRequestRepository.findLivroIdByPullRequest(idPullRequest)
                .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemIndisponivel));
        // O merge usa o mesmo lock para serializar alterações de sugestões e decisões.
        return livroRepository.findByIdForUpdate(idLivro)
                .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemIndisponivel));
    }

    private void exigirVisibilidade(Livro livro, UUID idUsuario, String mensagemIndisponivel) {
        if (livro == null || (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro()))) {
            throw new RecursoNaoEncontradoException(mensagemIndisponivel);
        }
    }

    private void exigirRevisor(Livro livro, UUID idUsuario) {
        ColaboraEm colaboracao = colaboraEmRepository
                .findByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro()).orElse(null);
        if (livro.getDono().getIdUsuario().equals(idUsuario)
                || colaboracao == null || colaboracao.getCargo() != CargoColab.REVISOR) {
            throw new AccessDeniedException("Somente revisores podem criar ou retirar sugestões.");
        }
    }

    private void exigirAberto(PullRequest pullRequest) {
        if (pullRequest.getStatus() != StatusPullRequest.ABERTA) {
            throw new ConflitoVersionamentoException("O Pull Request já foi encerrado.");
        }
    }

    private boolean dadosValidos(SugestaoRequestDTO dados) {
        return dados != null && dados.idCapitulo() != null
                && dados.categoria() != null && !dados.categoria().isBlank() && dados.categoria().length() <= 50
                && dados.trechoOriginal() != null && !dados.trechoOriginal().isBlank()
                && dados.trechoSugerido() != null && !dados.trechoSugerido().isBlank()
                && dados.justificativa() != null && !dados.justificativa().isBlank()
                && dados.posicaoInicial() != null && dados.posicaoInicial() >= 0
                && dados.posicaoFim() != null && dados.posicaoFim() > dados.posicaoInicial();
    }

    private SugestaoResponseDTO montarResposta(Sugestao sugestao) {
        return new SugestaoResponseDTO(sugestao.getIdSugestao(), sugestao.getCapitulo().getIdCapitulo(),
                sugestao.getUsuario().getIdUsuario(), sugestao.getPullRequest().getIdPullRequest(),
                sugestao.getCategoria(), sugestao.getTrechoOriginal(), sugestao.getTrechoSugerido(),
                sugestao.getJustificativa(), sugestao.getPosicaoInicial(), sugestao.getPosicaoFim(),
                sugestao.getStatus(), sugestao.getDataSugestao(), sugestao.getDataAvaliacao());
    }
}
