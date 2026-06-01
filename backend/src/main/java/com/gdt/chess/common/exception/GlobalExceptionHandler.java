package com.gdt.chess.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 *
 * <p>Uses RFC 7807 {@link ProblemDetail} so clients receive a consistent,
 * machine-readable error body regardless of which exception was raised.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleGameNotFound(GameNotFoundException ex) {
        log.warn("Game not found: {}", ex.getGameId());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("urn:chess:error:game-not-found"));
        pd.setTitle("Game Not Found");
        return pd;
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ProblemDetail handleInvalidMove(InvalidMoveException ex) {
        log.warn("Invalid move: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("urn:chess:error:invalid-move"));
        pd.setTitle("Invalid Move");
        return pd;
    }

    @ExceptionHandler(EngineException.class)
    public ProblemDetail handleEngineError(EngineException ex) {
        log.error("Engine error: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        pd.setType(URI.create("urn:chess:error:engine-unavailable"));
        pd.setTitle("Chess Engine Error");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
        pd.setType(URI.create("urn:chess:error:validation"));
        pd.setTitle("Validation Error");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setType(URI.create("urn:chess:error:internal"));
        pd.setTitle("Internal Server Error");
        return pd;
    }
}
