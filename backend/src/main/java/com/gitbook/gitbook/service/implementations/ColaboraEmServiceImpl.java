package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.ColaboraEmService;
import lombok.RequiredArgsConstructor;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.exceptions.ConflitoNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColaboraEmServiceImpl implements ColaboraEmService {

    private final ColaboraEmRepository colaboraEmRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;

    @Override
    @Transactional(readOnly = true)
    public ColaboraEmResponseDTO buscarPorId(UUID idColaboracao) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idColaboracao));
        return ColaboraEmResponseDTO.fromEntity(colaboracao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorLivro(UUID idLivro) {
        return colaboraEmRepository.findByLivro_IdLivro(idLivro)
                .stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorUsuario(UUID idUsuario) {
        return colaboraEmRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ColaboraEmResponseDTO cadastrar(ColaboraEmRequestDTO dto) {
        boolean jaColabora = colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(dto.idUsuario(), dto.idLivro());

        if (jaColabora) {
            throw new ConflitoNegocioException("O usuário já colabora neste livro.");
        }

        if (dto.cargo() == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        Usuario usuario = usuarioRepository.findById(dto.idUsuario()).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + dto.idUsuario()));
        Livro livro = livroRepository.findById(dto.idLivro()).orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado: " + dto.idLivro()));

        boolean ehDono = usuario.getIdUsuario().equals(livro.getDono().getIdUsuario());

        if (ehDono && dto.cargo() != CargoColab.GESTOR) {
            throw new IllegalArgumentException("O dono do livro deve possuir cargo GESTOR.");
        }

        if (!ehDono && dto.cargo() == CargoColab.GESTOR) {
            throw new IllegalArgumentException("Somente o dono do livro pode possuir cargo GESTOR.");
        }

        ColaboraEm colaboracao = new ColaboraEm(usuario, livro, dto.cargo());

        ColaboraEm salva = colaboraEmRepository.save(colaboracao);

        return ColaboraEmResponseDTO.fromEntity(salva);
    }

    @Transactional
    @Override
    public ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idColaboracao));

        boolean ehDono = colaboracao.getUsuario().getIdUsuario().equals(colaboracao.getLivro().getDono().getIdUsuario());

        if (ehDono) {
            throw new ConflitoNegocioException("A colaboração do dono não pode ser alterada.");
        }

        if (dto.cargo() == null || dto.cargo() == CargoColab.GESTOR) {
            throw new IllegalArgumentException("Informe um cargo válido diferente de GESTOR.");
        }

        colaboracao.setCargo(dto.cargo());

        ColaboraEm atualizada = colaboraEmRepository.save(colaboracao);

        return ColaboraEmResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public boolean remover(UUID idColaboracao) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idColaboracao));

        boolean ehDono = colaboracao.getUsuario().getIdUsuario().equals(colaboracao.getLivro().getDono().getIdUsuario());

        if (ehDono) {
            throw new ConflitoNegocioException("A colaboração do dono não pode ser excluída.");
        }

        colaboraEmRepository.delete(colaboracao);
        return true;
    }
}
