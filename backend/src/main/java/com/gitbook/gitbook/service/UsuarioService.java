package com.gitbook.gitbook.service;


import com.gitbook.gitbook.dtos.usuario.UsuarioRequestDTO;
import com.gitbook.gitbook.dtos.usuario.UsuarioResponseDTO;

import java.util.UUID;

public interface UsuarioService {
    public UsuarioResponseDTO buscarPorId(UUID idUsuario);
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO usuario);
    public UsuarioResponseDTO atualizarUsuario(UUID idUsuario, UsuarioRequestDTO usuario);
}
