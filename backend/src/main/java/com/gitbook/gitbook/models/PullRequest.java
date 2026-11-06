package com.gitbook.gitbook.models;

import com.gitbook.gitbook.models.enums.StatusPullRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pull_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pull_request", nullable = false, updatable = false)
    private UUID idPullRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    @Column(name = "id_branch_destino", nullable = false)
    private UUID idBranchDestino;

    @Column(name = "id_branch_origem", nullable = false)
    private UUID idBranchOrigem;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPullRequest status = StatusPullRequest.ABERTA;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @CreationTimestamp
    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit_origem", nullable = false, updatable = false)
    private Commit commitOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commit_destino_abertura", updatable = false)
    private Commit commitDestinoNaAbertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commit_merge")
    private Commit commitMerge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor_decisao")
    private Usuario autorDecisao;

    @Column(name = "justificativa_rejeicao", columnDefinition = "TEXT")
    private String justificativaRejeicao;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    public PullRequest(Usuario solicitante, Branch origem, Branch destino, String titulo, String descricao) {
        this.solicitante = Objects.requireNonNull(solicitante, "O solicitante é obrigatório.");
        Objects.requireNonNull(origem, "A branch de origem é obrigatória.");
        Objects.requireNonNull(destino, "A branch de destino é obrigatória.");
        if (origem == destino || Objects.equals(origem.getIdBranch(), destino.getIdBranch())) {
            throw new IllegalArgumentException("As branches de origem e destino devem ser diferentes.");
        }
        if (!origem.getLivro().getIdLivro().equals(destino.getLivro().getIdLivro())) {
            throw new IllegalArgumentException("As branches devem pertencer ao mesmo livro.");
        }
        if (titulo == null || titulo.isBlank() || titulo.length() > 150) {
            throw new IllegalArgumentException("O título deve ter entre 1 e 150 caracteres e não pode estar em branco.");
        }
        this.idBranchOrigem = Objects.requireNonNull(origem.getIdBranch());
        this.idBranchDestino = Objects.requireNonNull(destino.getIdBranch());
        this.commitOrigem = Objects.requireNonNull(origem.getCommitAtual(), "A origem precisa ter um commit.");
        this.commitDestinoNaAbertura = destino.getCommitAtual();
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public void aprovar(Usuario decisor, Commit commitMerge) {
        exigirAberto();
        Objects.requireNonNull(decisor, "O autor da decisão é obrigatório.");
        Objects.requireNonNull(commitMerge, "O commit de integração é obrigatório.");
        if (!commitOrigem.getIdLivro().equals(commitMerge.getIdLivro())
                || !idBranchDestino.equals(commitMerge.getIdBranch())) {
            throw new IllegalArgumentException("O commit de integração deve pertencer ao livro e à branch de destino.");
        }
        this.autorDecisao = decisor;
        this.commitMerge = commitMerge;
        this.status = StatusPullRequest.MERGEADA;
        this.dataFechamento = LocalDateTime.now();
    }

    public void rejeitar(Usuario decisor, String justificativa) {
        exigirAberto();
        Objects.requireNonNull(decisor, "O autor da decisão é obrigatório.");
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("A justificativa da rejeição é obrigatória.");
        }
        this.autorDecisao = decisor;
        this.justificativaRejeicao = justificativa;
        this.status = StatusPullRequest.REJEITADA;
        this.dataFechamento = LocalDateTime.now();
    }

    private void exigirAberto() {
        if (status != StatusPullRequest.ABERTA) {
            throw new IllegalStateException("O Pull Request já foi encerrado.");
        }
    }
}
