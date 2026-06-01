package com.gdt.chess.common.exception;

/**
 * Thrown when a move is rejected because it violates chess rules or is
 * syntactically malformed.
 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
