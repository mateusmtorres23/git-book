package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "conteudo_capitulo",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_conteudo_commit_capitulo", columnNames = {"id_commit", "id_capitulo"}),
                @UniqueConstraint(name = "uq_conteudo_commit_ordem", columnNames = {"id_commit", "ordem"})
        }
)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConteudoCapitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_versao", nullable = false, updatable = false)
    private UUID idVersao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit", nullable = false, updatable = false)
    private Commit commit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_capitulo", nullable = false, updatable = false)
    private Capitulo capitulo;

    @Size(max = 150)
    @Column(name = "titulo", length = 150, updatable = false)
    private String titulo;

    @Positive
    @Column(name = "ordem", updatable = false)
    private Integer ordem;

    @Column(name = "conteudo", columnDefinition = "TEXT", updatable = false)
    private String conteudo;

    @Column(name = "excluido", nullable = false, updatable = false)
    private boolean excluido;

    public ConteudoCapitulo(Commit commit, Capitulo capitulo, String titulo, Integer ordem, String conteudo) {
        this(commit, capitulo, titulo, ordem, conteudo, false);
    }

    private ConteudoCapitulo(Commit commit, Capitulo capitulo, String titulo, Integer ordem,
                            String conteudo, boolean excluido) {
        this.commit = Objects.requireNonNull(commit, "O commit do conteúdo é obrigatório.");
        this.capitulo = Objects.requireNonNull(capitulo, "O capítulo do conteúdo é obrigatório.");
        this.titulo = titulo;
        this.ordem = ordem;
        this.conteudo = conteudo;
        this.excluido = excluido;
        validarEstado();
    }

    public static ConteudoCapitulo excluir(Commit commit, Capitulo capitulo) {
        return new ConteudoCapitulo(commit, capitulo, null, null, null, true);
    }

    @PrePersist
    private void validarEstado() {
        Objects.requireNonNull(commit, "O commit do conteúdo é obrigatório.");
        Objects.requireNonNull(capitulo, "O capítulo do conteúdo é obrigatório.");
        if (capitulo.getLivro().getIdLivro() == null
                || !capitulo.getLivro().getIdLivro().equals(commit.getIdLivro())) {
            throw new IllegalArgumentException("O capítulo e o commit devem pertencer ao mesmo livro.");
        }
        if (excluido) {
            if (titulo != null || ordem != null || conteudo != null) {
                throw new IllegalArgumentException("Uma exclusão não deve carregar título, ordem ou texto.");
            }
            return;
        }
        if (titulo == null || titulo.isBlank() || titulo.length() > 150) {
            throw new IllegalArgumentException("O título do capítulo deve ter entre 1 e 150 caracteres e não pode estar em branco.");
        }
        if (ordem == null || ordem <= 0) {
            throw new IllegalArgumentException("A ordem do capítulo deve ser positiva.");
        }
        Objects.requireNonNull(conteudo, "O texto do capítulo é obrigatório.");
    }
}
