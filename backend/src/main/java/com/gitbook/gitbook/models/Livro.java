package com.gitbook.gitbook.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Column(name = "id_livro")
    private UUID idLivro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dono", nullable = false)
    private Usuario dono;

    @Size(max = 20)
    @Column(name = "versao", length = 20,nullable = false)
    private String versao;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome_livro", length = 150, nullable = false)
    private String nomeLivro;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false,updatable = false)
    private LocalDateTime dataCriacao;
}
