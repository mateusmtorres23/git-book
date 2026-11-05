package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {
    public List<Livro> findByDono (Usuario dono);
}
