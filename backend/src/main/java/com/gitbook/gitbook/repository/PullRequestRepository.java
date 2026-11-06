package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.PullRequest;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, UUID> {
    @Query("select b.livro.idLivro from PullRequest p, Branch b "
            + "where p.idBranchDestino = b.idBranch and p.idPullRequest = :idPullRequest")
    Optional<UUID> findLivroIdByPullRequest(@Param("idPullRequest") UUID idPullRequest);

    @Query("select p from PullRequest p, Branch b "
            + "where p.idBranchDestino = b.idBranch and b.livro.idLivro = :idLivro "
            + "order by p.dataAbertura desc")
    List<PullRequest> findByLivroId(@Param("idLivro") UUID idLivro);

    List<PullRequest> findByIdBranchOrigemOrderByDataAberturaDesc(UUID idBranchOrigem);

    List<PullRequest> findByIdBranchDestinoOrderByDataAberturaDesc(UUID idBranchDestino);

    List<PullRequest> findByIdBranchOrigemAndStatusOrderByDataAberturaDesc(UUID idBranchOrigem, StatusPullRequest status);

    List<PullRequest> findByIdBranchDestinoAndStatusOrderByDataAberturaDesc(UUID idBranchDestino, StatusPullRequest status);

    List<PullRequest> findBySolicitante_IdUsuarioOrderByDataAberturaDesc(UUID idSolicitante);
}
