package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.capitulo.CapituloResponseDTO;
import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.repository.BranchRepository;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.CapituloService;
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
    private final UsuarioRepository usuarioRepository;
    private final ColaboraEmRepository colaboraEmRepository;
    private final VersionamentoService versionamento;

    @Override
    public List<CapituloResponseDTO> buscarPorBranch(UUID idBranch, UUID idUsuario) {
        if (idBranch == null || idUsuario == null) {
            return null;
        }
        Branch branch = branchRepository.findById(idBranch).orElse(null);
        if (branch == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        Livro livro = branch.getLivro();
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            return null;
        }

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
        if (idCapitulo == null || idBranch == null || idUsuario == null) {
            return null;
        }
        Branch branch = branchRepository.findById(idBranch).orElse(null);
        if (branch == null || !usuarioRepository.existsById(idUsuario)) {
            return null;
        }
        Livro livro = branch.getLivro();
        if (!livro.getDono().getIdUsuario().equals(idUsuario)
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            return null;
        }

        EstadoCapitulo capitulo = versionamento.reconstruir(
                branch.getCommitAtual(), branch.getLivro().getIdLivro()).get(idCapitulo);
        if (capitulo == null) {
            return null;
        }
        return montarResposta(capitulo);
    }

    private CapituloResponseDTO montarResposta(EstadoCapitulo estado) {
        return new CapituloResponseDTO(estado.capitulo().getIdCapitulo(),
                estado.capitulo().getLivro().getIdLivro(), estado.titulo(), estado.ordem());
    }
}
