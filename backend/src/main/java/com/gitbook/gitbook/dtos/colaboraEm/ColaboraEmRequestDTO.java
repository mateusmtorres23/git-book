package com.gitbook.gitbook.dtos.colaboraEm;

import com.gitbook.gitbook.models.enums.CargoColab;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ColaboraEmRequestDTO(
        UUID idUsuario,
        @NotNull UUID idLivro,
        @NotNull CargoColab cargo,
        @Email @Size(max = 100) String email
) {
    public ColaboraEmRequestDTO(UUID idUsuario, UUID idLivro, CargoColab cargo) {
        this(idUsuario, idLivro, cargo, null);
    }

    @JsonIgnore
    @AssertTrue(message = "Informe exatamente um identificador de usuário: UUID ou e-mail não vazio.")
    public boolean isIdentificadorValido() {
        return idUsuario == null ? email != null && !email.isBlank() : email == null;
    }
}
