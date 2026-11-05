package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "conteudo_capitulo")
@Getter
@NoArgsConstructor
public class ConteudoCapitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_versao", nullable = false, updatable = false)
    private UUID idVersao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commit", nullable = false, updatable = false)
    private Commit commit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_capitulo", nullable = false, updatable = false)
    private Capitulo capitulo;

    @Column(name = "conteudo", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String conteudo;

    public ConteudoCapitulo(Commit commit, Capitulo capitulo, String conteudo) {
        this.commit = commit;
        this.capitulo = capitulo;
        this.conteudo = conteudo;
    }
}
