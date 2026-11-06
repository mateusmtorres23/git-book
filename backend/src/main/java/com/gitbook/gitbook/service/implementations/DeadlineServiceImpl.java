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
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl implements DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final ColaboraEmRepository colaboraEmRepository;

    @Override
    @Transactional(readOnly = true)
    public DeadlineResponseDTO buscarPorId(UUID idDeadline) {
        Deadline deadline = deadlineRepository.findById(idDeadline).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idDeadline));
        return DeadlineResponseDTO.fromEntity(deadline);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadlineResponseDTO> listarPorLivro(UUID idLivro) {
        return deadlineRepository.findByLivro_IdLivro(idLivro)
                .stream()
                .map(DeadlineResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadlineResponseDTO> listarPorUsuario(UUID idUsuario) {
        return deadlineRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(DeadlineResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public DeadlineResponseDTO cadastrar(DeadlineRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario()).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + dto.idUsuario()));
        Livro livro = livroRepository.findById(dto.idLivro()).orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado: " + dto.idLivro()));

        boolean ehDono = usuario.getIdUsuario()
                .equals(livro.getDono().getIdUsuario());

        boolean ehColaborador =
                colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(
                        dto.idUsuario(),
                        dto.idLivro()
                );

        if (!ehDono && !ehColaborador) {
            throw new IllegalArgumentException("O usuário deve ser dono ou colaborador do livro.");
        }

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
    public DeadlineResponseDTO atualizar(UUID idDeadline, DeadlineUpdateRequestDTO dto) {
        Deadline deadline = deadlineRepository.findById(idDeadline).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idDeadline));
        Usuario usuario = usuarioRepository.findById(dto.idUsuario()).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + dto.idUsuario()));
        Livro livro = deadline.getLivro();

        boolean ehDono = usuario.getIdUsuario()
                .equals(livro.getDono().getIdUsuario());

        boolean ehColaborador =
                colaboraEmRepository.existsByUsuario_IdUsuarioAndLivro_IdLivro(
                        dto.idUsuario(),
                        livro.getIdLivro()
                );

        if (!ehDono && !ehColaborador) {
            throw new IllegalArgumentException("O usuário deve ser dono ou colaborador do livro.");
        }

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
    public DeadlineResponseDTO atualizarStatus(UUID idDeadline, DeadlineStatusRequestDTO dto) {
        if (dto.status() == null) {
            throw new IllegalArgumentException("Dados incompatíveis com as regras da operação.");
        }

        Deadline deadline = deadlineRepository.findById(idDeadline).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idDeadline));
        deadline.atualizarStatus(dto.status());

        Deadline atualizada = deadlineRepository.save(deadline);

        return DeadlineResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public boolean remover(UUID idDeadline) {
        Deadline deadline = deadlineRepository.findById(idDeadline).orElseThrow(() -> new RecursoNaoEncontradoException("Recurso não encontrado: " + idDeadline));

        deadlineRepository.delete(deadline);

        return true;
    }
}
