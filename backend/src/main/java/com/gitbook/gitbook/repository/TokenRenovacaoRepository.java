package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.TokenRenovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenRenovacaoRepository extends JpaRepository<TokenRenovacao, UUID> {
    @Query("select t.sessao.idSessaoAuth from TokenRenovacao t where t.hashToken = :hashToken")
    Optional<UUID> findIdSessaoByHashToken(@Param("hashToken") String hashToken);

    Optional<TokenRenovacao> findByHashToken(String hashToken);
}
