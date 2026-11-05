package com.gitbook.gitbook.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id_livro;

    @ManyToOne
    @JoinColumn(name = "id_dono", nullable = false)
    private Usuario dono;

    @Column(name = "versao", length = 20,nullable = false)
    private String versao;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "nome_livro", length = 150, nullable = false)
    private String nome_livro;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false,updatable = false)
    private LocalDateTime data_criacao;
}
