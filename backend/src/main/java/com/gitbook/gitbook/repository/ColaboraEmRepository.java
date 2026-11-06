package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.ColaboraEm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaboraEmRepository extends JpaRepository<ColaboraEm, UUID> {
    Optional<ColaboraEm> findByUsuario_IdUsuarioAndLivro_IdLivro(UUID idUsuario, UUID idLivro);

    boolean existsByUsuario_IdUsuarioAndLivro_IdLivro(UUID idUsuario, UUID idLivro);

    List<ColaboraEm> findByLivro_IdLivro(UUID livroIdLivro);

    List<ColaboraEm> findByUsuario_IdUsuario(UUID usuarioIdUsuario);
}
