package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.sugestao.SugestaoRequestDTO;
import com.gitbook.gitbook.dtos.sugestao.SugestaoResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.PullRequest;
import com.gitbook.gitbook.models.Sugestao;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import com.gitbook.gitbook.models.enums.StatusSugestao;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.PullRequestRepository;
import com.gitbook.gitbook.repository.SugestaoRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.SugestaoService;
import com.gitbook.gitbook.service.support.VersionamentoService;
import com.gitbook.gitbook.service.support.VersionamentoService.EstadoCapitulo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class
SugestaoServiceImpl implements SugestaoService {
    private final SugestaoRepository sugestaoRepository;
    private final PullRequestRepository pullRequestRepository;
    private final LivroRepository livroRepository;
    private final BranchRepository branchRepository;
    private final UsuarioRepository usuarioRepository;
    private final ColaboraEmRepository colaboraEmRepository;
    private final VersionamentoService versionamento;

    @Override
    @Transactional
    public SugestaoResponseDTO adicionarSugestao(UUID idPullRequest, SugestaoRequestDTO dados, UUID idUsuario) {
        if (idPullRequest == null || idUsuario == null || !dadosValidos(dados)) {
            return null;
        }
        Livro livro = bloquearLivroDoPullRequest(idPullRequest);
        if (livro == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        if (livro.getDono().getIdUsuario().equals(idUsuario)) {
            return null;
        }
        ColaboraEm colaboracao = colaboraEmRepository
                .findByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro()).orElse(null);
        if (colaboracao == null || colaboracao.getCargo() != CargoColab.REVISOR) {
            return null;
        }

        PullRequest pullRequest = pullRequestRepository.findById(idPullRequest).orElse(null);
        if (pullRequest == null || pullRequest.getStatus() != StatusPullRequest.ABERTA
                || pullRequest.getCommitOrigem() == null
                || !livro.getIdLivro().equals(pullRequest.getCommitOrigem().getIdLivro())) {
            return null;
        }

        // Usa o commit fixado no PR, mesmo que a branch já tenha novos commits.
        EstadoCapitulo capitulo = versionamento.reconstruir(
                pullRequest.getCommitOrigem(), livro.getIdLivro()).get(dados.idCapitulo());
        if (capitulo == null || dados.posicaoFim() > capitulo.conteudo().length()) {
            return null;
        }
        String trecho = capitulo.conteudo().substring(dados.posicaoInicial(), dados.posicaoFim());
        if (!trecho.equals(dados.trechoOriginal())) {
            return null;
        }

        Sugestao sugestao = new Sugestao();
        sugestao.setCapitulo(capitulo.capitulo());
        sugestao.setUsuario(usuarioRepository.findById(idUsuario).orElse(null));
        if (sugestao.getUsuario() == null) {
            return null;
        }
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
        if (idSugestao == null || idUsuario == null) {
            return null;
        }
        Sugestao sugestao = sugestaoRepository.findById(idSugestao).orElse(null);
        if (sugestao == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        Livro livro = obterLivro(sugestao.getPullRequest());
        if (livro == null) {
            return null;
        }
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            return null;
        }
        return montarResposta(sugestao);
    }

    @Override
    public List<SugestaoResponseDTO> listarSugestoes(UUID idPullRequest, UUID idUsuario) {
        if (idPullRequest == null || idUsuario == null) {
            return null;
        }
        PullRequest pullRequest = pullRequestRepository.findById(idPullRequest).orElse(null);
        Livro livro = obterLivro(pullRequest);
        if (livro == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            return null;
        }

        List<SugestaoResponseDTO> respostas = new ArrayList<>();
        for (Sugestao sugestao : sugestaoRepository.findByPullRequest(pullRequest)) {
            respostas.add(montarResposta(sugestao));
        }
        return respostas;
    }

    @Override
    @Transactional
    public SugestaoResponseDTO retirarSugestao(UUID idSugestao, UUID idUsuario) {
        if (idSugestao == null || idUsuario == null) {
            return null;
        }
        UUID idPullRequest = sugestaoRepository.findPullRequestIdBySugestao(idSugestao).orElse(null);
        if (idPullRequest == null) {
            return null;
        }
        Livro livro = bloquearLivroDoPullRequest(idPullRequest);
        if (livro == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        if (livro.getDono().getIdUsuario().equals(idUsuario)) {
            return null;
        }
        ColaboraEm colaboracao = colaboraEmRepository
                .findByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro()).orElse(null);
        if (colaboracao == null || colaboracao.getCargo() != CargoColab.REVISOR) {
            return null;
        }

        // Carrega a sugestão após o lock para enxergar a última retirada confirmada.
        Sugestao sugestao = sugestaoRepository.findById(idSugestao).orElse(null);
        if (sugestao == null || !sugestao.getUsuario().getIdUsuario().equals(idUsuario)
                || sugestao.getPullRequest().getStatus() != StatusPullRequest.ABERTA) {
            return null;
        }
        if (sugestao.getStatus() == StatusSugestao.RETIRADA) {
            return montarResposta(sugestao);
        }

        sugestao.setStatus(StatusSugestao.RETIRADA);
        sugestao.setDataAvaliacao(LocalDateTime.now());
        return montarResposta(sugestaoRepository.saveAndFlush(sugestao));
    }

    private Livro obterLivro(PullRequest pullRequest) {
        if (pullRequest == null) {
            return null;
        }
        Branch destino = branchRepository.findById(pullRequest.getIdBranchDestino()).orElse(null);
        return destino == null ? null : destino.getLivro();
    }

    private Livro bloquearLivroDoPullRequest(UUID idPullRequest) {
        UUID idLivro = pullRequestRepository.findLivroIdByPullRequest(idPullRequest).orElse(null);
        if (idLivro == null) {
            return null;
        }
        // O merge usa o mesmo lock: criar ou retirar sugestões não disputa com a decisão.
        return livroRepository.findByIdForUpdate(idLivro).orElse(null);
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
