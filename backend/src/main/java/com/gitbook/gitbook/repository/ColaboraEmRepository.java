package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.ColaboraEm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ColaboraEmRepository extends JpaRepository<ColaboraEm, UUID> {
    boolean existsByUsuario_IdUsuarioAndLivro_IdLivro(UUID idUsuario, UUID idLivro);
}
