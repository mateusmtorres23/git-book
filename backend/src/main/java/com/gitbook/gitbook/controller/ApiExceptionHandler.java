package com.gitbook.gitbook.controller;

import com.gitbook.gitbook.service.exceptions.ConflitoVersionamentoException;
import com.gitbook.gitbook.service.exceptions.ConflitoNegocioException;
import com.gitbook.gitbook.service.exceptions.RecursoNaoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Erros de conversão, JSON e @Valid são tratados pela implementação MVC herdada. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException erro,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problema = problema(HttpStatus.BAD_REQUEST, "Os dados informados são inválidos.");
        problema.setProperty("erros", erro.getBindingResult().getAllErrors().stream()
                .map(falha -> falha.getDefaultMessage()).distinct().sorted().toList());
        return handleExceptionInternal(erro, problema, headers, status, request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException erro) {
        return problema(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler({ConflitoVersionamentoException.class, ConflitoNegocioException.class})
    public ProblemDetail conflito(RuntimeException erro) {
        return problema(HttpStatus.CONFLICT, erro.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    public ProblemDetail conflitoPersistencia(RuntimeException erro) {
        return problema(HttpStatus.CONFLICT, "A operação conflita com o estado atual dos dados. Atualize e tente novamente.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(IllegalArgumentException erro) {
        return problema(HttpStatus.BAD_REQUEST, erro.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail validacao(ConstraintViolationException erro) {
        var problema = problema(HttpStatus.BAD_REQUEST, "Os dados informados são inválidos.");
        problema.setProperty("erros", erro.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage()).sorted().toList());
        return problema;
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail naoAutenticado(AuthenticationCredentialsNotFoundException erro) {
        return problema(HttpStatus.UNAUTHORIZED, erro.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail acessoNegado(AccessDeniedException erro) {
        return problema(HttpStatus.FORBIDDEN, erro.getMessage());
    }

    private ProblemDetail problema(HttpStatus status, String detalhe) {
        return ProblemDetail.forStatusAndDetail(status, detalhe);
    }
}
