package com.forthix.forthic.errors;

public class UnterminatedStringError extends RuntimeException {
    public UnterminatedStringError(String message, CodeLocation location) {
        super(message);
    }
}
