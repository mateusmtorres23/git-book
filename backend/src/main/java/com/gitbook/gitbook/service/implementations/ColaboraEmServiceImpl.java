package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.ColaboraEmRequestDTO;
import com.gitbook.gitbook.dtos.ColaboraEmResponseDTO;
import com.gitbook.gitbook.dtos.ColaboraEmUpdateRequestDTO;
import com.gitbook.gitbook.models.ColaboraEm;
import com.gitbook.gitbook.models.Livro;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.models.enums.CargoColab;
import com.gitbook.gitbook.repository.ColaboraEmRepository;
import com.gitbook.gitbook.repository.LivroRepository;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.ColaboraEmService;
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
    private final LivroRepository livroRepository;

    @Override
    @Transactional(readOnly = true)
    public ColaboraEmResponseDTO buscarPorId(UUID idColaboracao) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).get();
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
            return null;
        }

        if (dto.cargo() == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findById(dto.idUsuario()).get();
        Livro livro = livroRepository.findById(dto.idLivro()).get();

        boolean ehDono = usuario.getIdUsuario().equals(livro.getDono().getIdUsuario());

        if (ehDono && dto.cargo() != CargoColab.GESTOR) {
            return null;
        }

        if (!ehDono && dto.cargo() == CargoColab.GESTOR) {
            return null;
        }

        ColaboraEm colaboracao = new ColaboraEm(usuario, livro, dto.cargo());

        ColaboraEm salva = colaboraEmRepository.save(colaboracao);

        return ColaboraEmResponseDTO.fromEntity(salva);
    }

    @Transactional
    @Override
    public ColaboraEmResponseDTO atualizarCargo(UUID idColaboracao, ColaboraEmUpdateRequestDTO dto) {
        if (dto.cargo() == null || dto.cargo() == CargoColab.GESTOR) {
            return null;
        }

        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).get();

        boolean ehDono = colaboracao.getUsuario().getIdUsuario().equals(colaboracao.getLivro().getDono().getIdUsuario());

        if (ehDono) {
            return null;
        }

        colaboracao.setCargo(dto.cargo());

        ColaboraEm atualizada = colaboraEmRepository.save(colaboracao);

        return ColaboraEmResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public boolean remover(UUID idColaboracao) {
        ColaboraEm colaboracao = colaboraEmRepository.findById(idColaboracao).get();

        boolean ehDono = colaboracao.getUsuario().getIdUsuario().equals(colaboracao.getLivro().getDono().getIdUsuario());

        if (ehDono) {
            return false;
        }

        colaboraEmRepository.delete(colaboracao);
        return true;
    }
}
