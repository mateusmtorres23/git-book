package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.deadline.DeadlineRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineStatusRequestDTO;
import com.gitbook.gitbook.dtos.deadline.DeadlineUpdateRequestDTO;
import com.gitbook.gitbook.models.Deadline;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.DeadlineRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.DeadlineService;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.AcessoLivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl implements DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcessoLivroService acesso;
    private final ColaboraEmRepository colaboraEmRepository;

    @Override
    @Transactional(readOnly = true)
    public DeadlineResponseDTO buscarPorId(UUID idDeadline, UUID idSolicitante) {
        Deadline deadline = obterPrazo(idDeadline);
        acesso.exigirLeitura(deadline.getLivro(), idSolicitante);
        return DeadlineResponseDTO.fromEntity(deadline);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadlineResponseDTO> listarPorLivro(UUID idLivro, UUID idSolicitante) {
        acesso.exigirLeitura(acesso.obterLivro(idLivro), idSolicitante);
        return deadlineRepository.findByLivro_IdLivro(idLivro)
                .stream()
                .map(DeadlineResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadlineResponseDTO> listarPorUsuario(UUID idUsuario, UUID idSolicitante) {
        acesso.obterUsuario(idSolicitante);
        return deadlineRepository.findVisiveisPorUsuario(idUsuario, idSolicitante)
                .stream()
                .map(DeadlineResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public DeadlineResponseDTO cadastrar(DeadlineRequestDTO dto, UUID idSolicitante) {
        Livro livro = acesso.bloquearLivro(dto.idLivro());
        acesso.exigirGestor(livro, idSolicitante);
        Usuario usuario = obterResponsavel(dto.idUsuario(), livro);

        Deadline deadline = new Deadline(
                usuario,
                livro,
                dto.titulo(),
                dto.descricao(),
                dto.dataLimite()
        );

        Deadline salva = deadlineRepository.save(deadline);

        return DeadlineResponseDTO.fromEntity(salva);
    }

    @Override
    @Transactional
    public DeadlineResponseDTO atualizar(UUID idDeadline, DeadlineUpdateRequestDTO dto, UUID idSolicitante) {
        Livro livro = bloquearLivroDoPrazo(idDeadline);
        acesso.exigirGestor(livro, idSolicitante);
        Deadline deadline = obterPrazo(idDeadline);
        Usuario usuario = obterResponsavel(dto.idUsuario(), livro);

        deadline.atualizarDados(
                usuario,
                dto.titulo(),
                dto.descricao(),
                dto.dataLimite()
        );

        Deadline atualizada = deadlineRepository.save(deadline);

        return DeadlineResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public DeadlineResponseDTO atualizarStatus(UUID idDeadline, DeadlineStatusRequestDTO dto, UUID idSolicitante) {
        if (dto.status() == null) {
            throw new IllegalArgumentException("Dados incompatíveis com as regras da operação.");
        }

        Livro livro = bloquearLivroDoPrazo(idDeadline);
        acesso.exigirLeitura(livro, idSolicitante);
        Deadline deadline = obterPrazo(idDeadline);
        if (!idSolicitante.equals(livro.getDono().getIdUsuario())
                && !idSolicitante.equals(deadline.getUsuario().getIdUsuario())) {
            throw new AccessDeniedException("Você só pode atualizar o andamento das suas próprias entregas.");
        }
        deadline.atualizarStatus(dto.status());

        Deadline atualizada = deadlineRepository.save(deadline);

        return DeadlineResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public boolean remover(UUID idDeadline, UUID idSolicitante) {
        Livro livro = bloquearLivroDoPrazo(idDeadline);
        acesso.exigirGestor(livro, idSolicitante);
        Deadline deadline = obterPrazo(idDeadline);
        deadlineRepository.delete(deadline);

        return true;
    }

    private Livro bloquearLivroDoPrazo(UUID idDeadline) {
        UUID idLivro = deadlineRepository.findIdLivroByIdDeadline(idDeadline)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prazo não encontrado."));
        return acesso.bloquearLivro(idLivro);
    }

    private Deadline obterPrazo(UUID idDeadline) {
        return deadlineRepository.findById(idDeadline)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prazo não encontrado."));
    }

    private Usuario obterResponsavel(UUID idUsuario, Livro livro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + idUsuario));
        if (!idUsuario.equals(livro.getDono().getIdUsuario())
                && !colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(idUsuario, livro.getIdLivro())) {
            throw new IllegalArgumentException("O usuário deve ser dono ou colaborador do livro.");
        }
        return usuario;
    }
}
