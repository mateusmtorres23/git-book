package com.gitbook.gitbook.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO (
    @NotBlank @Size(max = 100)
    String nomeUsuario,
    @NotBlank @Email @Size(max = 100)
    String email,
    @NotBlank @Size(min = 8)
    String senha
){

}