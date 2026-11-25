package com.gitbook.gitbook.service.support;

import java.util.Locale;

public final class EmailUsuario {
    private EmailUsuario() {
    }

    public static String normalizar(String email) {
        String normalizado = email.toLowerCase(Locale.ROOT);
        if (normalizado.length() > 100) {
            throw new IllegalArgumentException("O e-mail deve ter até 100 caracteres após a normalização.");
        }
        return normalizado;
    }
}
