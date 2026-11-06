package com.gitbook.gitbook.service.support;

import com.gitbook.gitbook.models.Branch;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AcessoLivroService {
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ColaboraEmRepository colaboraEmRepository;

    public Livro obterLivro(UUID idLivro) {
        return livroRepository.findById(idLivro)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
    }

    public Livro bloquearLivro(UUID idLivro) {
        return livroRepository.findByIdForUpdate(idLivro)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
    }

    public Usuario obterUsuario(UUID idUsuario) {
        if (idUsuario == null) {
            throw new AuthenticationCredentialsNotFoundException("É necessário estar autenticado.");
        }
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário autenticado não encontrado."));
    }

    public void exigirLeitura(Livro livro, UUID idUsuario) {
        obterUsuario(idUsuario);
        if (!ehDono(livro, idUsuario) && colaboracao(livro, idUsuario) == null) {
            throw new AccessDeniedException("Você não participa deste livro.");
        }
    }

    public void exigirGestor(Livro livro, UUID idUsuario) {
        obterUsuario(idUsuario);
        if (!ehDono(livro, idUsuario)) {
            throw new AccessDeniedException("Esta operação exige o dono do livro.");
        }
    }

    public void exigirCriacaoBranch(Livro livro, UUID idUsuario) {
        exigirLeitura(livro, idUsuario);
        if (!ehDono(livro, idUsuario) && cargo(livro, idUsuario) != CargoColab.ESCRITOR) {
            throw new AccessDeniedException("Somente o dono e escritores podem criar branches.");
        }
    }

    public void exigirEscrita(Livro livro, Branch branch, UUID idUsuario) {
        exigirLeitura(livro, idUsuario);
        if (ehDono(livro, idUsuario)) {
            return;
        }
        if (branch.isMain() || cargo(livro, idUsuario) != CargoColab.ESCRITOR
                || !branch.getAutor().getIdUsuario().equals(idUsuario)) {
            throw new AccessDeniedException("Você só pode escrever na sua própria branch de trabalho.");
        }
    }

    public void exigirDecisaoPullRequest(Livro livro, UUID idUsuario) {
        exigirLeitura(livro, idUsuario);
        if (!ehDono(livro, idUsuario) && cargo(livro, idUsuario) != CargoColab.REVISOR) {
            throw new AccessDeniedException("Somente o dono e revisores podem decidir Pull Requests.");
        }
    }

    private boolean ehDono(Livro livro, UUID idUsuario) {
        return livro.getDono().getIdUsuario().equals(idUsuario);
    }

    private CargoColab cargo(Livro livro, UUID idUsuario) {
        ColaboraEm colaboracao = colaboracao(livro, idUsuario);
        return colaboracao == null ? null : colaboracao.getCargo();
    }

    private ColaboraEm colaboracao(Livro livro, UUID idUsuario) {
        return colaboraEmRepository.findByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro()).orElse(null);
    }
}
