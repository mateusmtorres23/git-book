package com.gitbook.gitbook.models;

import com.gitbook.gitbook.models.enums.StatusSugestao;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sugestao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_sugestao")
    private UUID idSugestao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_capitulo", nullable = false)
    private Capitulo capitulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pull_request", nullable = false)
    private PullRequest pullRequest;

    @Size(max = 50)
    @Column(name = "categoria", length = 50, nullable = false)
    private String categoria;

    @Column(name = "trecho_original", nullable = false, columnDefinition = "TEXT")
    private String trechoOriginal;

    @Column(name = "trecho_sugerido", nullable = false, columnDefinition = "TEXT")
    private String trechoSugerido;

    @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "posicao_inicial", nullable = false)
    private Integer posicaoInicial;

    @Column(name = "posicao_fim", nullable = false)
    private Integer posicaoFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusSugestao status = StatusSugestao.PENDENTE;

    @CreationTimestamp
    @Column(name = "data_sugestao", nullable = false, updatable = false)
    private LocalDateTime dataSugestao;

    @Column(name = "data_avaliacao")
    private LocalDateTime dataAvaliacao;
}
