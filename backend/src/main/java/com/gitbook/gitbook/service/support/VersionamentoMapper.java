package com.gitbook.gitbook.service.support;

import com.gitbook.gitbook.dtos.branch.BranchResponseDTO;
import com.gitbook.gitbook.dtos.capitulo.ConteudoCapituloResponseDTO;
import com.gitbook.gitbook.dtos.commit.CommitPaiResponseDTO;
import com.gitbook.gitbook.dtos.commit.CommitResponseDTO;
import com.gitbook.gitbook.dtos.pullrequest.PullRequestResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.Commit;
import com.gitbook.gitbook.models.CommitPai;
import com.gitbook.gitbook.models.ConteudoCapitulo;
import com.gitbook.gitbook.models.PullRequest;

import java.util.UUID;

public final class VersionamentoMapper {
    private VersionamentoMapper() {
    }

    public static UUID id(Commit commit) {
        return commit == null ? null : commit.getIdCommit();
    }

    public static BranchResponseDTO branch(Branch branch) {
        return new BranchResponseDTO(branch.getIdBranch(), branch.getLivro().getIdLivro(),
                branch.getAutor().getIdUsuario(), branch.getNome(), branch.isMain(), id(branch.getCommitAtual()),
                branch.getDataCriacao());
    }

    public static CommitResponseDTO commit(Commit commit) {
        return new CommitResponseDTO(commit.getIdCommit(), commit.getIdLivro(), commit.getIdAutor(),
                commit.getIdBranch(), commit.getMensagem(), commit.getDataCriacao());
    }

    public static CommitPaiResponseDTO pai(CommitPai pai) {
        return new CommitPaiResponseDTO(pai.getIdPaternidade(), pai.getCommitFilho().getIdCommit(),
                pai.getCommitPai().getIdCommit(), pai.getOrdemPai());
    }

    public static ConteudoCapituloResponseDTO conteudo(ConteudoCapitulo conteudo) {
        return new ConteudoCapituloResponseDTO(conteudo.getIdVersao(), conteudo.getCommit().getIdCommit(),
                conteudo.getCapitulo().getIdCapitulo(), conteudo.getTitulo(), conteudo.getOrdem(),
                conteudo.getConteudo(), conteudo.isExcluido());
    }

    public static PullRequestResponseDTO pullRequest(PullRequest pr) {
        return new PullRequestResponseDTO(pr.getIdPullRequest(), pr.getSolicitante().getIdUsuario(),
                pr.getIdBranchOrigem(), pr.getIdBranchDestino(), pr.getTitulo(), pr.getDescricao(), pr.getStatus(),
                pr.getDataAbertura(), id(pr.getCommitOrigem()), id(pr.getCommitDestinoNaAbertura()),
                id(pr.getCommitMerge()), pr.getAutorDecisao() == null ? null : pr.getAutorDecisao().getIdUsuario(),
                pr.getJustificativaRejeicao(), pr.getDataFechamento());
    }
}
