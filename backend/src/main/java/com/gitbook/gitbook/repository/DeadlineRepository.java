package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeadlineRepository extends JpaRepository<Deadline, UUID> {
    List<Deadline> findByLivro_IdLivro(UUID livroIdLivro);

    List<Deadline> findByUsuario_IdUsuario(UUID usuarioIdUsuario);
}
