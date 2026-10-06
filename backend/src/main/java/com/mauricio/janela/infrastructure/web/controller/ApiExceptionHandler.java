package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.exception.ExternalServiceUnavailableException;
import com.mauricio.janela.domain.exception.LocationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Maps failures to RFC 9457 problem details with generic messages; internals stay in the logs. Extending
 * {@link ResponseEntityExceptionHandler} gives Spring's own errors (unknown route, wrong method, bad version) the
 * same shape as ours.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(LocationNotFoundException.class)
    ProblemDetail handleLocationNotFound(LocationNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "City not found.");
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    ProblemDetail handleExternalServiceUnavailable(ExternalServiceUnavailableException e) {
        log.warn("External service unavailable", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Weather data is temporarily unavailable. Please try again later.");
    }

    /** Query parameters bound to a record ({@code @ModelAttribute}) fail here; the problem lists the fields. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(invalidParameters(e));
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleBindException(BindException e) {
        return invalidParameters(e);
    }

    private static ProblemDetail invalidParameters(BindException e) {
        List<String> fields = e.getFieldErrors().stream().map(FieldError::getField).distinct().sorted().toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters.");
        problem.setProperty("fields", fields);
        return problem;
    }
}
