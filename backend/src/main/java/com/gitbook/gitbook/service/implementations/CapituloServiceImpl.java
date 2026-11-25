package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.capitulo.CapituloResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.service.CapituloService;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.VersionamentoService;
import com.gitbook.gitbook.service.support.VersionamentoService.EstadoCapitulo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CapituloServiceImpl implements CapituloService {
    private final BranchRepository branchRepository;
    private final AcessoLivroService acesso;
    private final ColaboraEmRepository colaboraEmRepository;
    private final VersionamentoService versionamento;

    @Override
    public List<CapituloResponseDTO> buscarPorBranch(UUID idBranch, UUID idUsuario) {
        Branch branch = obterBranchVisivel(idBranch, idUsuario);

        List<EstadoCapitulo> capitulos = new ArrayList<>(versionamento.reconstruir(
                branch.getCommitAtual(), branch.getLivro().getIdLivro()).values());
        capitulos.sort(Comparator.comparing(EstadoCapitulo::ordem));

        List<CapituloResponseDTO> respostas = new ArrayList<>();
        for (EstadoCapitulo capitulo : capitulos) {
            respostas.add(montarResposta(capitulo));
        }
        return respostas;
    }

    @Override
    public CapituloResponseDTO buscarCapitulo(UUID idCapitulo, UUID idUsuario, UUID idBranch) {
        Branch branch = obterBranchVisivel(idBranch, idUsuario);

        EstadoCapitulo capitulo = versionamento.reconstruir(
                branch.getCommitAtual(), branch.getLivro().getIdLivro()).get(idCapitulo);
        if (capitulo == null) {
            throw new RecursoNaoEncontradoException("Capítulo não encontrado.");
        }
        return montarResposta(capitulo);
    }

    private Branch obterBranchVisivel(UUID idBranch, UUID idUsuario) {
        acesso.obterUsuario(idUsuario);
        Branch branch = branchRepository.findById(idBranch)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Branch não encontrada."));
        Livro livro = branch.getLivro();
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            throw new RecursoNaoEncontradoException("Branch não encontrada.");
        }
        return branch;
    }

    private CapituloResponseDTO montarResposta(EstadoCapitulo estado) {
        return new CapituloResponseDTO(estado.capitulo().getIdCapitulo(),
                estado.capitulo().getLivro().getIdLivro(), estado.titulo(), estado.ordem());
    }
}
