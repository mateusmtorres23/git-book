package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommitRepository extends JpaRepository<Commit, UUID> {
    List<Commit> findByIdLivroOrderByDataCriacaoDesc(UUID idLivro);

    List<Commit> findByIdBranchOrderByDataCriacaoDesc(UUID idBranch);

    Optional<Commit> findByIdCommitAndIdLivro(UUID idCommit, UUID idLivro);
}
