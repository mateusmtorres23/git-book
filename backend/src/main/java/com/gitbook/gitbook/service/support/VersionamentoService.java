package com.gitbook.gitbook.service.support;

import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloRequestDTO;
import com.gitbook.gitbook.dtos.commit.CommitDetalheResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.models.*;
import com.gitbook.gitbook.repository.*;
import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VersionamentoService {
    private final CommitRepository commitRepository;
    private final CommitPaiRepository commitPaiRepository;
    private final ConteudoCapituloRepository conteudoRepository;
    private final CapituloRepository capituloRepository;
    private final BranchRepository branchRepository;

    public record EstadoCapitulo(Capitulo capitulo, String titulo, Integer ordem, String conteudo,
                                 ConteudoCapitulo registro) {
    }

    public record Alteracao(Capitulo capitulo, String titulo, Integer ordem, String conteudo, boolean excluido) {
    }

    private record Grafo(Map<UUID, Commit> commits, Map<UUID, List<CommitPai>> pais) {
    }

    public Commit obterCommit(UUID idCommit) {
        return commitRepository.findById(idCommit)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Commit não encontrado."));
    }

    public Branch obterBranch(UUID idBranch) {
        return branchRepository.findById(idBranch)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Branch não encontrada."));
    }

    public UUID livroDaBranch(UUID idBranch) {
        return branchRepository.findLivroIdByBranch(idBranch)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Branch não encontrada."));
    }

    public List<CommitPai> pais(Commit commit) {
        return carregarGrafo(commit, commit.getIdLivro()).pais().get(commit.getIdCommit());
    }

    public List<Commit> historico(Commit ponta) {
        if (ponta == null) {
            return List.of();
        }
        return carregarGrafo(ponta, ponta.getIdLivro()).commits().values().stream()
                .sorted(Comparator.comparing(Commit::getDataCriacao,
                        Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(Commit::getIdCommit))
                .toList();
    }

    public List<Commit> linhaPrincipal(Commit ponta) {
        if (ponta == null) {
            return List.of();
        }
        Grafo grafo = carregarGrafo(ponta, ponta.getIdLivro());
        List<Commit> linha = new ArrayList<>();
        Commit atual = ponta;
        while (atual != null) {
            linha.add(atual);
            List<CommitPai> pais = grafo.pais().get(atual.getIdCommit());
            atual = pais.isEmpty() ? null : pais.get(0).getCommitPai();
        }
        return List.copyOf(linha);
    }

    public Map<UUID, EstadoCapitulo> reconstruir(Commit ponta, UUID idLivro) {
        Grafo grafo = carregarGrafo(ponta, idLivro);
        Set<UUID> encontrados = new HashSet<>();
        Map<UUID, EstadoCapitulo> estado = new HashMap<>();
        Commit atual = ponta;
        while (atual != null) {
            Set<UUID> locais = new HashSet<>();
            for (ConteudoCapitulo registro : conteudoRepository.findByCommit_IdCommitOrderByOrdemAsc(atual.getIdCommit())) {
                UUID idCapitulo = registro.getCapitulo().getIdCapitulo();
                if (idCapitulo == null || !locais.add(idCapitulo)
                        || !idLivro.equals(registro.getCapitulo().getLivro().getIdLivro())
                        || !atual.getIdCommit().equals(registro.getCommit().getIdCommit())) {
                    throw new IllegalStateException("Conteúdo histórico inconsistente.");
                }
                if (encontrados.add(idCapitulo) && !registro.isExcluido()) {
                    estado.put(idCapitulo, new EstadoCapitulo(registro.getCapitulo(), registro.getTitulo(),
                            registro.getOrdem(), registro.getConteudo(), registro));
                }
            }
            List<CommitPai> pais = grafo.pais().get(atual.getIdCommit());
            atual = pais.isEmpty() ? null : pais.get(0).getCommitPai();
        }
        validarPosicoes(estado);
        return estado;
    }

    public VersaoLivroResponseDTO versao(UUID idLivro, Commit commit) {
        var capitulos = reconstruir(commit, idLivro).values().stream()
                .sorted(Comparator.comparing(EstadoCapitulo::ordem))
                .map(estado -> VersionamentoMapper.conteudo(estado.registro())).toList();
        return new VersaoLivroResponseDTO(idLivro, VersionamentoMapper.id(commit), capitulos);
    }

    public CommitDetalheResponseDTO detalhe(Commit commit) {
        return new CommitDetalheResponseDTO(VersionamentoMapper.commit(commit),
                pais(commit).stream().map(VersionamentoMapper::pai).toList(),
                conteudoRepository.findByCommit_IdCommitOrderByOrdemAsc(commit.getIdCommit()).stream()
                        .map(VersionamentoMapper::conteudo).toList());
    }

    public List<Alteracao> planejarAlteracoes(Map<UUID, EstadoCapitulo> base,
                                             List<ConteudoCapituloRequestDTO> pedidos) {
        Map<UUID, EstadoCapitulo> resultado = new HashMap<>(base);
        List<Alteracao> alteracoes = new ArrayList<>();
        for (ConteudoCapituloRequestDTO pedido : pedidos) {
            EstadoCapitulo anterior = pedido.idCapitulo() == null ? null : base.get(pedido.idCapitulo());
            if (pedido.idCapitulo() != null && anterior == null) {
                throw new IllegalArgumentException("O capítulo informado não está presente na versão de base.");
            }
            if (pedido.excluido()) {
                resultado.remove(pedido.idCapitulo());
                alteracoes.add(new Alteracao(anterior.capitulo(), null, null, null, true));
            } else {
                EstadoCapitulo proposto = new EstadoCapitulo(anterior == null ? null : anterior.capitulo(),
                        pedido.titulo(), pedido.ordem(), pedido.conteudo(), null);
                if (!iguais(anterior, proposto)) {
                    UUID chave = pedido.idCapitulo() == null ? UUID.randomUUID() : pedido.idCapitulo();
                    resultado.put(chave, proposto);
                    alteracoes.add(new Alteracao(proposto.capitulo(), proposto.titulo(), proposto.ordem(),
                            proposto.conteudo(), false));
                }
            }
        }
        validarPosicoes(resultado);
        if (alteracoes.isEmpty()) {
            throw new IllegalArgumentException("Não há alterações para registrar.");
        }
        return List.copyOf(alteracoes);
    }

    public Commit baseComum(Commit origem, Commit destino, UUID idLivro) {
        Grafo grafoOrigem = carregarGrafo(origem, idLivro);
        Grafo grafoDestino = carregarGrafo(destino, idLivro);
        Set<UUID> comuns = new HashSet<>(grafoOrigem.commits().keySet());
        comuns.retainAll(grafoDestino.commits().keySet());
        Set<UUID> melhores = new HashSet<>(comuns);
        for (UUID id : comuns) {
            for (CommitPai pai : grafoOrigem.pais().get(id)) {
                melhores.remove(pai.getCommitPai().getIdCommit());
            }
        }
        if (melhores.size() > 1) {
            throw new ConflitoVersionamentoException("O histórico possui múltiplas bases de merge; a integração exige revisão.");
        }
        return melhores.isEmpty() ? null : grafoOrigem.commits().get(melhores.iterator().next());
    }

    public Map<UUID, EstadoCapitulo> combinar(Map<UUID, EstadoCapitulo> base,
                                             Map<UUID, EstadoCapitulo> origem,
                                             Map<UUID, EstadoCapitulo> destino) {
        Set<UUID> ids = new HashSet<>(base.keySet());
        ids.addAll(origem.keySet());
        ids.addAll(destino.keySet());
        Map<UUID, EstadoCapitulo> resultado = new HashMap<>();
        for (UUID id : ids) {
            EstadoCapitulo anterior = base.get(id);
            EstadoCapitulo enviado = origem.get(id);
            EstadoCapitulo oficial = destino.get(id);
            EstadoCapitulo combinado;
            if (iguais(enviado, anterior)) {
                combinado = oficial;
            } else if (iguais(oficial, anterior) || iguais(enviado, oficial)) {
                combinado = enviado;
            } else {
                if (anterior == null || enviado == null || oficial == null) {
                    throw conflito(id);
                }
                combinado = new EstadoCapitulo(oficial.capitulo(),
                        combinarCampo(anterior.titulo(), enviado.titulo(), oficial.titulo(), id),
                        combinarCampo(anterior.ordem(), enviado.ordem(), oficial.ordem(), id),
                        combinarCampo(anterior.conteudo(), enviado.conteudo(), oficial.conteudo(), id), null);
            }
            if (combinado != null) {
                resultado.put(id, combinado);
            }
        }
        try {
            validarPosicoes(resultado);
        } catch (IllegalArgumentException erro) {
            throw new ConflitoVersionamentoException("A integração gera posições de capítulos inválidas ou repetidas.");
        }
        return resultado;
    }

    public boolean mesmosConteudos(Map<UUID, EstadoCapitulo> primeiro, Map<UUID, EstadoCapitulo> segundo) {
        return primeiro.keySet().equals(segundo.keySet())
                && primeiro.entrySet().stream().allMatch(entrada -> iguais(entrada.getValue(), segundo.get(entrada.getKey())));
    }

    public List<Alteracao> diferencas(Map<UUID, EstadoCapitulo> anterior, Map<UUID, EstadoCapitulo> resultado) {
        Set<UUID> ids = new HashSet<>(anterior.keySet());
        ids.addAll(resultado.keySet());
        List<Alteracao> alteracoes = new ArrayList<>();
        for (UUID id : ids) {
            EstadoCapitulo antes = anterior.get(id);
            EstadoCapitulo depois = resultado.get(id);
            if (!iguais(antes, depois)) {
                alteracoes.add(depois == null
                        ? new Alteracao(antes.capitulo(), null, null, null, true)
                        : new Alteracao(depois.capitulo(), depois.titulo(), depois.ordem(), depois.conteudo(), false));
            }
        }
        return List.copyOf(alteracoes);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Commit gravar(Livro livro, Branch branch, Usuario autor, String mensagem,
                         List<Alteracao> alteracoes, Commit primeiroPai, Commit segundoPai) {
        // O chamador mantém o lock do livro durante toda a operação.
        if (!livro.getIdLivro().equals(branch.getLivro().getIdLivro())) {
            throw new IllegalArgumentException("A branch não pertence ao livro do commit.");
        }
        if (segundoPai != null && (primeiroPai == null
                || segundoPai.getIdCommit().equals(primeiroPai.getIdCommit()))) {
            throw new IllegalArgumentException("O merge exige dois pais distintos.");
        }
        if ((primeiroPai != null && !livro.getIdLivro().equals(primeiroPai.getIdLivro()))
                || (segundoPai != null && !livro.getIdLivro().equals(segundoPai.getIdLivro()))) {
            throw new IllegalArgumentException("Os pais devem pertencer ao mesmo livro.");
        }
        if (branch.getCommitAtual() != null
                && !Objects.equals(branch.getCommitAtual().getIdCommit(), VersionamentoMapper.id(primeiroPai))) {
            throw new ConflitoVersionamentoException("A base do novo commit não corresponde à ponta da branch.");
        }
        Commit novo = commitRepository.saveAndFlush(new Commit(livro.getIdLivro(), autor.getIdUsuario(),
                branch.getIdBranch(), mensagem));
        for (Alteracao alteracao : alteracoes) {
            Capitulo capitulo = alteracao.capitulo() == null
                    ? capituloRepository.save(new Capitulo(livro)) : alteracao.capitulo();
            conteudoRepository.save(alteracao.excluido() ? ConteudoCapitulo.excluir(novo, capitulo)
                    : new ConteudoCapitulo(novo, capitulo, alteracao.titulo(), alteracao.ordem(), alteracao.conteudo()));
        }
        if (primeiroPai != null) {
            commitPaiRepository.save(new CommitPai(novo, primeiroPai, 1));
        }
        if (segundoPai != null) {
            commitPaiRepository.save(new CommitPai(novo, segundoPai, 2));
        }
        branch.setCommitAtual(novo);
        branchRepository.save(branch);
        return novo;
    }

    private Grafo carregarGrafo(Commit ponta, UUID idLivro) {
        Map<UUID, Commit> commits = new HashMap<>();
        Map<UUID, List<CommitPai>> pais = new HashMap<>();
        Deque<Commit> pendentes = new ArrayDeque<>();
        if (ponta != null) {
            pendentes.add(ponta);
        }
        while (!pendentes.isEmpty()) {
            Commit commit = pendentes.removeFirst();
            if (!idLivro.equals(commit.getIdLivro()) || commit.getIdCommit() == null) {
                throw new IllegalStateException("Commit histórico não pertence ao livro esperado.");
            }
            if (commits.putIfAbsent(commit.getIdCommit(), commit) != null) {
                continue;
            }
            List<CommitPai> vinculos = commitPaiRepository.findByCommitFilho_IdCommitOrderByOrdemPaiAsc(commit.getIdCommit());
            Set<Integer> ordens = new HashSet<>();
            Set<UUID> idsPais = new HashSet<>();
            for (CommitPai vinculo : vinculos) {
                Integer ordem = vinculo.getOrdemPai();
                Commit pai = vinculo.getCommitPai();
                if (ordem == null || ordem < 1 || ordem > 2 || !ordens.add(ordem)
                        || !commit.getIdCommit().equals(vinculo.getCommitFilho().getIdCommit())
                        || !idLivro.equals(pai.getIdLivro()) || pai.getIdCommit() == null
                        || !idsPais.add(pai.getIdCommit())) {
                    throw new IllegalStateException("Relação de ancestralidade inconsistente.");
                }
                pendentes.add(pai);
            }
            if (ordens.contains(2) && !ordens.contains(1)) {
                throw new IllegalStateException("O histórico possui segundo pai sem primeiro pai.");
            }
            pais.put(commit.getIdCommit(), vinculos.stream()
                    .sorted(Comparator.comparing(CommitPai::getOrdemPai)).toList());
        }
        validarAusenciaDeCiclos(commits, pais);
        return new Grafo(commits, pais);
    }

    private void validarAusenciaDeCiclos(Map<UUID, Commit> commits, Map<UUID, List<CommitPai>> pais) {
        Map<UUID, Integer> entradas = new HashMap<>();
        commits.keySet().forEach(id -> entradas.put(id, 0));
        pais.values().forEach(vinculos -> vinculos.forEach(vinculo ->
                entradas.merge(vinculo.getCommitPai().getIdCommit(), 1, Integer::sum)));
        Deque<UUID> livres = new ArrayDeque<>();
        entradas.forEach((id, quantidade) -> { if (quantidade == 0) livres.add(id); });
        int visitados = 0;
        while (!livres.isEmpty()) {
            UUID id = livres.removeFirst();
            visitados++;
            for (CommitPai pai : pais.get(id)) {
                UUID idPai = pai.getCommitPai().getIdCommit();
                if (entradas.merge(idPai, -1, Integer::sum) == 0) {
                    livres.add(idPai);
                }
            }
        }
        if (visitados != commits.size()) {
            throw new IllegalStateException("O histórico de commits contém um ciclo.");
        }
    }

    private void validarPosicoes(Map<UUID, EstadoCapitulo> estado) {
        Set<Integer> posicoes = new HashSet<>();
        for (EstadoCapitulo capitulo : estado.values()) {
            if (capitulo.ordem() == null || capitulo.ordem() <= 0 || !posicoes.add(capitulo.ordem())
                    || capitulo.titulo() == null || capitulo.titulo().isBlank() || capitulo.conteudo() == null) {
                throw new IllegalArgumentException("O livro resultante precisa de capítulos válidos e posições positivas distintas.");
            }
        }
    }

    private boolean iguais(EstadoCapitulo primeiro, EstadoCapitulo segundo) {
        if (primeiro == null || segundo == null) {
            return primeiro == segundo;
        }
        return Objects.equals(primeiro.titulo(), segundo.titulo()) && Objects.equals(primeiro.ordem(), segundo.ordem())
                && Objects.equals(primeiro.conteudo(), segundo.conteudo());
    }

    private <T> T combinarCampo(T base, T origem, T destino, UUID idCapitulo) {
        if (Objects.equals(origem, base)) return destino;
        if (Objects.equals(destino, base) || Objects.equals(origem, destino)) return origem;
        throw conflito(idCapitulo);
    }

    private ConflitoVersionamentoException conflito(UUID idCapitulo) {
        return new ConflitoVersionamentoException("Há conflito no capítulo " + idCapitulo + ".");
    }
}
