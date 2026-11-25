package com.gitbook.gitbook.dtos.auth;

import java.time.Instant;

public record TokenAcessoResponseDTO(String token, Instant expiraEm) {
    @Override
    public String toString() {
        return "TokenAcessoResponseDTO[expiraEm=" + expiraEm + "]";
    }
}
