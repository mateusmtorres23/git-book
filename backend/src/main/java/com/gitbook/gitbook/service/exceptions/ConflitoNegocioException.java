package com.gitbook.gitbook.service.exceptions;

public class ConflitoNegocioException extends RuntimeException {
    public ConflitoNegocioException(String mensagem) {
        super(mensagem);
    }
}
