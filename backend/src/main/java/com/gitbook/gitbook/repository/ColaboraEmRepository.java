package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.ColaboraEm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaboraEmRepository extends JpaRepository<ColaboraEm, UUID> {
    Optional<ColaboraEm> findByUsuario_IdUsuarioAndLivro_IdLivro(UUID idUsuario, UUID idLivro);

    boolean existsByUsuario_IdUsuarioAndLivro_IdLivro(UUID idUsuario, UUID idLivro);

    List<ColaboraEm> findByLivro_IdLivro(UUID livroIdLivro);

    List<ColaboraEm> findByUsuario_IdUsuario(UUID usuarioIdUsuario);

    List<ColaboraEm> findByUsuario_IdUsuarioAndLivro_Dono_IdUsuario(UUID idUsuario, UUID idDono);

    @Query("select c.livro.idLivro from ColaboraEm c where c.idColaboracao = :idColaboracao")
    Optional<UUID> findIdLivroByIdColaboracao(@Param("idColaboracao") UUID idColaboracao);
}
