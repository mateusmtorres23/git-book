package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.CommitPai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommitPaiRepository extends JpaRepository<CommitPai, UUID> {
    List<CommitPai> findByCommitFilho_IdCommitOrderByOrdemPaiAsc(UUID idCommitFilho);

    Optional<CommitPai> findByCommitFilho_IdCommitAndOrdemPai(UUID idCommitFilho, Integer ordemPai);

    List<CommitPai> findByCommitPai_IdCommit(UUID idCommitPai);

    boolean existsByCommitFilho_IdCommitAndCommitPai_IdCommit(UUID idCommitFilho, UUID idCommitPai);
}
