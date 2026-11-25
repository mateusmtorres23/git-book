package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeadlineRepository extends JpaRepository<Deadline, UUID> {
    List<Deadline> findByLivro_IdLivro(UUID livroIdLivro);

    @Query("""
            select d from Deadline d
            where d.usuario.idUsuario = :idUsuario
              and (d.livro.dono.idUsuario = :idSolicitante or exists (
                    select c.idColaboracao from ColaboraEm c
                    where c.livro = d.livro and c.usuario.idUsuario = :idSolicitante))
            """)
    List<Deadline> findVisiveisPorUsuario(@Param("idUsuario") UUID idUsuario,
            @Param("idSolicitante") UUID idSolicitante);

    @Query("select d.livro.idLivro from Deadline d where d.idDeadline = :idDeadline")
    Optional<UUID> findIdLivroByIdDeadline(@Param("idDeadline") UUID idDeadline);
}
