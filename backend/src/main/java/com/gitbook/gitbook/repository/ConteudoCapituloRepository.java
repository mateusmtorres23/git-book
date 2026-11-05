package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.ConteudoCapitulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConteudoCapituloRepository extends JpaRepository<ConteudoCapitulo, UUID> {
}
