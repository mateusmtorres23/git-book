package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Capitulo;
import com.gitbook.gitbook.models.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapituloRepository extends JpaRepository<Capitulo, UUID> {
    List<Capitulo> findByLivro(Livro livro);

    Optional<Capitulo> findByIdCapituloAndLivro_IdLivro(UUID idCapitulo, UUID idLivro);
}
