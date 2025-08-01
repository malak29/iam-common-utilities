package com.iam.common.exception;

import com.iam.common.response.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomExceptions.UserNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUserNotFound(CustomExceptions.UserNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(CustomExceptions.UnAuthorizedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUnauthorized(CustomExceptions.UnAuthorizedException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(CustomExceptions.InvalidTokenException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleInvalidToken(CustomExceptions.InvalidTokenException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationErrors(WebExchangeBindException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errors)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGeneral(Exception ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred")));
    }
}