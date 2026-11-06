package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {
    public List<Livro> findByDono (Usuario dono);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Livro l where l.idLivro = :idLivro")
    Optional<Livro> findByIdForUpdate(@Param("idLivro") UUID idLivro);
}
