package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.exception.LocationNotFoundException;
import com.mauricio.janela.domain.exception.WeatherUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/**
 * Maps failures to RFC 9457 problem details with generic messages; internals stay in the logs.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(LocationNotFoundException.class)
    ProblemDetail handleLocationNotFound(LocationNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "City not found.");
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleInvalidRequest(BindException e) {
        List<String> fields = e.getFieldErrors().stream().map(FieldError::getField).distinct().sorted().toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters.");
        problem.setProperty("fields", fields);
        return problem;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleInvalidRequest(HandlerMethodValidationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters.");
    }

    @ExceptionHandler(WeatherUnavailableException.class)
    ProblemDetail handleWeatherUnavailable(WeatherUnavailableException e) {
        log.warn("Weather source unavailable", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Weather data is temporarily unavailable. Please try again later.");
    }
}
