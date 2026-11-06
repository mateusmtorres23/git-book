package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.PullRequest;
import com.gitbook.gitbook.models.enums.StatusPullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, UUID> {
    List<PullRequest> findByIdBranchOrigemOrderByDataAberturaDesc(UUID idBranchOrigem);

    List<PullRequest> findByIdBranchDestinoOrderByDataAberturaDesc(UUID idBranchDestino);

    List<PullRequest> findByIdBranchOrigemAndStatusOrderByDataAberturaDesc(UUID idBranchOrigem, StatusPullRequest status);

    List<PullRequest> findByIdBranchDestinoAndStatusOrderByDataAberturaDesc(UUID idBranchDestino, StatusPullRequest status);

    List<PullRequest> findBySolicitante_IdUsuarioOrderByDataAberturaDesc(UUID idSolicitante);
}
