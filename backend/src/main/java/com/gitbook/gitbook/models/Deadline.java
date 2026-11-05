package com.gitbook.gitbook.models;

import com.gitbook.gitbook.models.enums.StatusTask;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "Deadline"
)
public class Deadline {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "id_livro")
    private Livro livro;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2048)
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusTask status;


}
