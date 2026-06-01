package com.gdt.chess.common.exception;

/**
 * Thrown when the chess engine is unavailable, times out, or returns an
 * unexpected response.
 */
public class EngineException extends RuntimeException {

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
