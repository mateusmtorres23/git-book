package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.usuario.UsuarioRequestDTO;
import com.gitbook.gitbook.dtos.usuario.UsuarioResponseDTO;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.SessaoAuthService;
import com.gitbook.gitbook.service.UsuarioService;
import com.gitbook.gitbook.service.exceptions.ConflitoNegocioException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import com.gitbook.gitbook.service.support.EmailUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessaoAuthService sessaoAuthService;

    @Override
    public UsuarioResponseDTO buscarPorId(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNomeUsuario(),
                usuario.getEmail(),
                usuario.getDataCriacao()
        );
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        String email = EmailUsuario.normalizar(dto.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoNegocioException("O e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(dto.nomeUsuario());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        return new UsuarioResponseDTO(
                usuarioSalvo.getIdUsuario(),
                usuarioSalvo.getNomeUsuario(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getDataCriacao()
        );
    }

    public boolean verificarEmail(String email) {
        return usuarioRepository.existsByEmail(EmailUsuario.normalizar(email));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID idUsuario, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByIdForUpdate(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        String email = EmailUsuario.normalizar(dto.email());
        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw new ConflitoNegocioException("O e-mail já está cadastrado.");
        }

        usuario.setNomeUsuario(dto.nomeUsuario());
        usuario.setEmail(email);
        boolean senhaAlterada = !passwordEncoder.matches(dto.senha(), usuario.getSenhaHash());
        if (senhaAlterada) {
            usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
        }

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);
        if (senhaAlterada) {
            sessaoAuthService.revogarSessoesDoUsuario(idUsuario);
        }

        return new UsuarioResponseDTO(
                usuarioSalvo.getIdUsuario(),
                usuarioSalvo.getNomeUsuario(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getDataCriacao()
        );
    }
}
