package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.VersaoAtual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersaoAtualRepository extends JpaRepository<VersaoAtual, UUID> {
    Optional<VersaoAtual> findByCapitulo_IdCapitulo(UUID capituloIdCapitulo);
}
