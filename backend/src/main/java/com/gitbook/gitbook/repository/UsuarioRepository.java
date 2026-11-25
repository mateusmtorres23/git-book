package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);

    @Query("select u.idUsuario from Usuario u where u.email = :email")
    Optional<UUID> findIdByEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.idUsuario = :idUsuario")
    Optional<Usuario> findByIdForUpdate(@Param("idUsuario") UUID idUsuario);
}
