package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.deadline.DeadlineResponseDTO;
import com.gitbook.gitbook.models.Deadline;
import com.gitbook.gitbook.repository.DeadlineRepository;
import com.gitbook.gitbook.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl implements DeadlineService {

    private final DeadlineRepository deadlineRepository;

    @Override
    @Transactional(readOnly = true)
    public DeadlineResponseDTO buscarPorId(UUID idDeadline) {
        Deadline deadline = deadlineRepository.findById(idDeadline).get();
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
}
