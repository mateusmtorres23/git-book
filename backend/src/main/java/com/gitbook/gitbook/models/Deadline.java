package com.gitbook.gitbook.models;

import com.gitbook.gitbook.models.enums.StatusTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deadlines")
@Getter
@NoArgsConstructor
public class Deadline {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_deadline", nullable = false, updatable = false)
    private UUID idDeadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn( name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusTask status;

    @Column(name = "data_limite", nullable = false)
    private LocalDateTime dataLimite;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Deadline(Usuario usuario, Livro livro, String titulo, String descricao, LocalDateTime dataLimite) {
        this.usuario = usuario;
        this.livro = livro;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = StatusTask.PENDENTE;
        this.dataLimite = dataLimite;
    }

    public void atualizarStatus(StatusTask status) {
        this.status = status;
    }

    public void atualizarDados(
            Usuario usuario,
            String titulo,
            String descricao,
            LocalDateTime dataLimite
    ) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
    }
}
