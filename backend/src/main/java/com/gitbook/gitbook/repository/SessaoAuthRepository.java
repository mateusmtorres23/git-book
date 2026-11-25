package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.SessaoAuth;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessaoAuthRepository extends JpaRepository<SessaoAuth, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessaoAuth s where s.idSessaoAuth = :idSessaoAuth")
    Optional<SessaoAuth> findByIdForUpdate(@Param("idSessaoAuth") UUID idSessaoAuth);

    @Modifying(flushAutomatically = true)
    @Query("""
            update SessaoAuth s set s.revogadaEm = :agora
            where s.usuario.idUsuario = :idUsuario
              and s.revogadaEm is null
              and s.expiraEm > :agora
            """)
    int revogarAtivasDoUsuario(@Param("idUsuario") UUID idUsuario, @Param("agora") Instant agora);
}
