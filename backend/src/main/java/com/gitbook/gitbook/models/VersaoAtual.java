package com.gitbook.gitbook.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "versao_atual")
@Getter
@NoArgsConstructor
public class VersaoAtual {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_versao_atual", nullable = false, updatable = false)
    private UUID idVersaoAtual;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_capitulo", nullable = false, unique = true, updatable = false)
    private Capitulo capitulo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conteudo", nullable = false, unique = true)
    private ConteudoCapitulo conteudo;

    public VersaoAtual(Capitulo capitulo, ConteudoCapitulo conteudo) {
        this.capitulo = capitulo;
        this.conteudo = conteudo;
    }

    public void atualizarConteudo(ConteudoCapitulo conteudo) {
        this.conteudo = conteudo;
    }
}