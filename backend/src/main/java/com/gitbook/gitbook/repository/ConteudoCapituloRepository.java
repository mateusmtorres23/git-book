package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.ConteudoCapitulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConteudoCapituloRepository extends JpaRepository<ConteudoCapitulo, UUID> {
    List<ConteudoCapitulo> findByCapitulo_IdCapitulo(UUID capituloIdCapitulo);

    List<ConteudoCapitulo> findByCommit_IdCommit(UUID commitIdCommit);
}
