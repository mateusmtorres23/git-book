package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByLivro_IdLivro(UUID idLivro);

    List<Branch> findByLivro_IdLivroAndAutor_IdUsuario(UUID idLivro, UUID idAutor);

    Optional<Branch> findByLivro_IdLivroAndMainTrue(UUID idLivro);

    Optional<Branch> findByLivro_IdLivroAndNome(UUID idLivro, String nome);

    boolean existsByLivro_IdLivroAndNome(UUID idLivro, String nome);
}
