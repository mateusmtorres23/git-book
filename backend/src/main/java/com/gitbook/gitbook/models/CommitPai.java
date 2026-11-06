package com.gitbook.gitbook.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "commit_pai",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_commit_pai_relacao", columnNames = {"id_commit_filho", "id_commit_pai"}),
                @UniqueConstraint(name = "uq_commit_pai_ordem", columnNames = {"id_commit_filho", "ordem_pai"})
        }
)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommitPai {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_paternidade", nullable = false, updatable = false)
    private UUID idPaternidade;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit_filho", nullable = false, updatable = false)
    private Commit commitFilho;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit_pai", nullable = false, updatable = false)
    private Commit commitPai;

    @NotNull
    @Min(1)
    @Max(2)
    @Column(name = "ordem_pai", nullable = false, updatable = false)
    private Integer ordemPai;

    public CommitPai(Commit commitFilho, Commit commitPai, Integer ordemPai) {
        this.commitFilho = Objects.requireNonNull(commitFilho, "O commit filho é obrigatório.");
        this.commitPai = Objects.requireNonNull(commitPai, "O commit pai é obrigatório.");
        if (ordemPai == null || ordemPai < 1 || ordemPai > 2) {
            throw new IllegalArgumentException("A ordem do pai deve ser 1 ou 2.");
        }
        if (commitFilho == commitPai || (commitFilho.getIdCommit() != null
                && commitFilho.getIdCommit().equals(commitPai.getIdCommit()))) {
            throw new IllegalArgumentException("Um commit não pode ser pai de si mesmo.");
        }
        if (!commitFilho.getIdLivro().equals(commitPai.getIdLivro())) {
            throw new IllegalArgumentException("Os commits filho e pai devem pertencer ao mesmo livro.");
        }
        this.ordemPai = ordemPai;
    }
}
