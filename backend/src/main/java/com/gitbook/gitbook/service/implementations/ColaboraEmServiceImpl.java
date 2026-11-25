package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.colaboraEm.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.ColaboraEmService;
import com.gitbook.gitbook.service.exceptions.ConflitoNegocioException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import com.gitbook.gitbook.service.support.EmailUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColaboraEmServiceImpl implements ColaboraEmService {

    private final ColaboraEmRepository colaboraEmRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcessoLivroService acesso;

    @Override
    @Transactional(readOnly = true)
    public ColaboraEmResponseDTO buscarPorId(UUID idColaboracao, UUID idSolicitante) {
        acesso.obterUsuario(idSolicitante);
        ColaboraEm colaboracao = obterColaboracao(idColaboracao);
        if (!idSolicitante.equals(colaboracao.getUsuario().getIdUsuario())
                && !idSolicitante.equals(colaboracao.getLivro().getDono().getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Colaboração não encontrada.");
        }
        return ColaboraEmResponseDTO.fromEntity(colaboracao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorLivro(UUID idLivro, UUID idSolicitante) {
        acesso.obterUsuario(idSolicitante);
        Livro livro = acesso.obterLivro(idLivro);
        if (!idSolicitante.equals(livro.getDono().getIdUsuario())) {
            ColaboraEm vinculo = colaboraEmRepository.findByUsuario_IdUsuarioAndLivro_IdLivro(idSolicitante, idLivro)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
            return List.of(ColaboraEmResponseDTO.fromEntity(vinculo));
        }
        return colaboraEmRepository.findByLivro_IdLivro(idLivro)
                .stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaboraEmResponseDTO> listarPorUsuario(UUID idUsuario, UUID idSolicitante) {
        acesso.obterUsuario(idSolicitante);
        List<ColaboraEm> vinculos = idSolicitante.equals(idUsuario)
                ? colaboraEmRepository.findByUsuario_IdUsuario(idUsuario)
                : colaboraEmRepository.findByUsuario_IdUsuarioAndLivro_Dono_IdUsuario(idUsuario, idSolicitante);
        return vinculos.stream()
                .map(ColaboraEmResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ColaboraEmResponseDTO cadastrar(ColaboraEmRequestDTO dto, UUID idSolicitante) {
        Livro livro = acesso.bloquearLivro(dto.idLivro());
        acesso.exigirGestor(livro, idSolicitante);
        if (!dto.isIdentificadorValido()) {
            throw new IllegalArgumentException("Informe exatamente um identificador de usuário: UUID ou e-mail não vazio.");
        }
        Usuario usuario = localizarUsuario(dto);
        boolean jaColabora = colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(
                usuario.getIdUsuario(), dto.idLivro());

        if (jaColabora) {
            throw new ConflitoNegocioException("O usuário já colabora neste livro.");
        }

        if (dto.cargo() == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

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

    private Usuario localizarUsuario(ColaboraEmRequestDTO dto) {
        if (dto.idUsuario() != null) {
            return usuarioRepository.findById(dto.idUsuario())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + dto.idUsuario()));
        }
        String email = EmailUsuario.normalizar(dto.email());
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma conta foi encontrada para o e-mail informado."));
    }

    @Transactional
    @Override
    public ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto, UUID idSolicitante) {
        ColaboraEm colaboracao = obterColaboracaoParaGestao(idColaboracao, idSolicitante);

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
    public boolean remover(UUID idColaboracao, UUID idSolicitante) {
        ColaboraEm colaboracao = obterColaboracaoParaGestao(idColaboracao, idSolicitante);

        boolean ehDono = colaboracao.getUsuario().getIdUsuario().equals(colaboracao.getLivro().getDono().getIdUsuario());

        if (ehDono) {
            throw new ConflitoNegocioException("A colaboração do dono não pode ser excluída.");
        }

        colaboraEmRepository.delete(colaboracao);
        return true;
    }

    private ColaboraEm obterColaboracaoParaGestao(UUID idColaboracao, UUID idSolicitante) {
        UUID idLivro = colaboraEmRepository.findIdLivroByIdColaboracao(idColaboracao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaboração não encontrada."));
        Livro livro = acesso.bloquearLivro(idLivro);
        acesso.exigirGestor(livro, idSolicitante);
        return obterColaboracao(idColaboracao);
    }

    private ColaboraEm obterColaboracao(UUID idColaboracao) {
        return colaboraEmRepository.findById(idColaboracao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaboração não encontrada."));
    }
}
