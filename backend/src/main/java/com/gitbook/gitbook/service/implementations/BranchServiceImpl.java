package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.branch.BranchRequestDTO;
import com.gitbook.gitbook.dtos.branch.BranchResponseDTO;
import com.gitbook.gitbook.dtos.livro.VersaoLivroResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.Commit;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.service.BranchService;
import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.ValidacaoService;
import com.gitbook.gitbook.service.support.VersionamentoMapper;
import com.gitbook.gitbook.service.support.VersionamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final AcessoLivroService acesso;
    private final VersionamentoService versionamento;
    private final ValidacaoService validacao;

    @Override
    @Transactional
    public BranchResponseDTO criarMain(UUID idLivro, UUID idUsuario) {
        Livro livro = acesso.bloquearLivro(idLivro);
        acesso.exigirGestor(livro, idUsuario);
        var existente = branchRepository.findByLivro_IdLivroAndMainTrue(idLivro);
        if (existente.isPresent()) {
            return VersionamentoMapper.branch(existente.get());
        }
        if (branchRepository.existsByLivro_IdLivroAndNome(idLivro, "main")) {
            throw new ConflitoVersionamentoException("Já existe uma branch com o nome reservado main.");
        }
        Branch main = new Branch();
        main.setLivro(livro);
        main.setAutor(acesso.obterUsuario(idUsuario));
        main.setNome("main");
        main.setMain(true);
        return VersionamentoMapper.branch(branchRepository.saveAndFlush(main));
    }

    @Override
    @Transactional
    public BranchResponseDTO criar(UUID idLivro, BranchRequestDTO dados, UUID idUsuario) {
        validacao.validar(dados);
        Livro livro = acesso.bloquearLivro(idLivro);
        acesso.exigirCriacaoBranch(livro, idUsuario);
        String nome = dados.nome().strip();
        if (nome.equalsIgnoreCase("main")) {
            throw new IllegalArgumentException("O nome main é reservado.");
        }
        if (branchRepository.existsByLivro_IdLivroAndNome(idLivro, nome)) {
            throw new ConflitoVersionamentoException("Já existe uma branch com esse nome no livro.");
        }
        Branch main = branchRepository.findByLivro_IdLivroAndMainTrue(idLivro)
                .orElseThrow(() -> new IllegalStateException("O livro ainda não possui uma main."));
        Commit origem = main.getCommitAtual();
        if (dados.idCommitOrigem() != null) {
            origem = versionamento.linhaPrincipal(main.getCommitAtual()).stream()
                    .filter(commit -> commit.getIdCommit().equals(dados.idCommitOrigem())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("O commit de origem deve fazer parte da linha principal da main."));
        }
        Branch branch = new Branch();
        branch.setLivro(livro);
        branch.setAutor(acesso.obterUsuario(idUsuario));
        branch.setNome(nome);
        branch.setMain(false);
        branch.setCommitAtual(origem);
        return VersionamentoMapper.branch(branchRepository.saveAndFlush(branch));
    }

    @Override
    public BranchResponseDTO buscarPorId(UUID idBranch, UUID idUsuario) {
        Branch branch = versionamento.obterBranch(idBranch);
        acesso.exigirLeitura(branch.getLivro(), idUsuario);
        return VersionamentoMapper.branch(branch);
    }

    @Override
    public List<BranchResponseDTO> listarPorLivro(UUID idLivro, UUID idUsuario) {
        acesso.exigirLeitura(acesso.obterLivro(idLivro), idUsuario);
        return branchRepository.findByLivro_IdLivro(idLivro).stream()
                .sorted(Comparator.comparing(Branch::isMain).reversed().thenComparing(Branch::getNome))
                .map(VersionamentoMapper::branch).toList();
    }

    @Override
    public VersaoLivroResponseDTO consultarConteudo(UUID idBranch, UUID idUsuario) {
        Branch branch = versionamento.obterBranch(idBranch);
        acesso.exigirLeitura(branch.getLivro(), idUsuario);
        return versionamento.versao(branch.getLivro().getIdLivro(), branch.getCommitAtual());
    }
}
