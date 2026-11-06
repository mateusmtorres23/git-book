package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Capitulo;
import com.gitbook.gitbook.models.PullRequest;
import com.gitbook.gitbook.models.enums.StatusSugestao;
import com.gitbook.gitbook.models.Sugestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SugestaoRepository extends JpaRepository<Sugestao, UUID> {
    @Query("select s.pullRequest.idPullRequest from Sugestao s where s.idSugestao = :idSugestao")
    Optional<UUID> findPullRequestIdBySugestao(@Param("idSugestao") UUID idSugestao);

    public List<Sugestao> findByCapitulo (Capitulo capitulo);
    public List<Sugestao> findByCapituloAndStatus (Capitulo capitulo, StatusSugestao statusSugestao);
    public List<Sugestao> findByPullRequest (PullRequest pullRequest);
    List<Sugestao> findByPullRequestAndStatus(PullRequest pullRequest, StatusSugestao status);
    boolean existsByPullRequestAndStatus(PullRequest pullRequest, StatusSugestao status);
}
