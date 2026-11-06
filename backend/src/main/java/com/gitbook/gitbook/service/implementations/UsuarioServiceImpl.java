package com.gitbook.gitbook.service.implementations;

import com.gitbook.gitbook.dtos.usuario.UsuarioRequestDTO;
import com.gitbook.gitbook.dtos.usuario.UsuarioResponseDTO;
import com.gitbook.gitbook.models.Usuario;
import com.gitbook.gitbook.repository.UsuarioRepository;
import com.gitbook.gitbook.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UsuarioResponseDTO buscarPorId(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {
            return null;
        }

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
        if (verificarEmail(dto.email())) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(dto.nomeUsuario());
        usuario.setEmail(dto.email());
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
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID idUsuario, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {
            return null;
        }

        if (!usuario.getEmail().equals(dto.email()) && verificarEmail(dto.email())) {
            return null;
        }

        usuario.setNomeUsuario(dto.nomeUsuario());
        usuario.setEmail(dto.email());
        usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        return new UsuarioResponseDTO(
                usuarioSalvo.getIdUsuario(),
                usuarioSalvo.getNomeUsuario(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getDataCriacao()
        );
    }
}
