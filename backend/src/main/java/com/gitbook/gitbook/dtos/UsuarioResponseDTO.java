package com.gitbook.gitbook.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID idUsuario,
        String nomeUsuario,
        String email,
        LocalDateTime dataCriacao
) {
}
