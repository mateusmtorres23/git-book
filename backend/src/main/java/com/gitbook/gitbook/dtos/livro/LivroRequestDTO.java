package com.gitbook.gitbook.dtos.livro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroRequestDTO (
    @NotBlank @Size(max = 150)
    String nomeLivro,
    String descricao
) {

}
